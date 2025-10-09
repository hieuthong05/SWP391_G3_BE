package BE.service;

import BE.entity.*;
import BE.model.DTO.AvailableTimeSlotsDTO;
import BE.model.DTO.TimeSlotDTO;
import BE.model.request.BookingRequest;
import BE.model.response.BookingResponse;
import BE.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    @Autowired
    OrdersRepository ordersRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    ServiceCenterRepository serviceCenterRepository;

    @Autowired
    ServicesRepository servicesRepository;

    @Autowired
    ServicePackageRepository servicePackageRepository;

    @Autowired
    ModelMapper modelMapper;

    private static final int MAX_BOOKINGS_PER_SLOT = 8;
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(20, 0);
    private static final int SLOT_DURATION_MINUTES = 60;

    //Lấy danh sách time slots available cho 1 ngày

    public AvailableTimeSlotsDTO getAvailableTimeSlots(Long serviceCenterId, LocalDate date)
    {
        // Validate service center
        ServiceCenter center = serviceCenterRepository.findById(serviceCenterId)
                .orElseThrow(() -> new EntityNotFoundException("Service center not found"));

        List<TimeSlotDTO> timeSlots = new ArrayList<>();
        LocalTime currentSlot = OPENING_TIME;

        while (currentSlot.isBefore(CLOSING_TIME))
        {
            LocalTime endTime = currentSlot.plusMinutes(SLOT_DURATION_MINUTES);

            // Đếm số booking trong slot này
            int bookingsCount = ordersRepository.countBookingsInTimeSlot(
                    serviceCenterId, date, currentSlot);

            int remainingSlots = MAX_BOOKINGS_PER_SLOT - bookingsCount;
            boolean available = remainingSlots > 0;

            timeSlots.add(new TimeSlotDTO(currentSlot, endTime, available, remainingSlots));
            currentSlot = endTime;
        }

        return new AvailableTimeSlotsDTO(date, timeSlots);
    }

    //Tạo booking mới

    @Transactional
    public BookingResponse createBooking(BookingRequest dto)
    {
        List<String> errors = new ArrayList<>();

        // 1. Validate: phải chọn service HOẶC package
        if ((dto.getServiceId() == null && dto.getPackageId() == null) ||
             (dto.getServiceId() != null && dto.getPackageId() != null))
        {
            errors.add("Please select either a service or a package, not both");
        }

        // 2. Validate entities exist
        Customer customer = null;
        Vehicle vehicle = null;
        ServiceCenter serviceCenter = null;
        BE.entity.Service service = null;
        ServicePackage servicePackage = null;

        try {
            customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        } catch (EntityNotFoundException e) {
            errors.add("Customer not found with ID: " + dto.getCustomerId());
        }

        try {
            vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
        } catch (EntityNotFoundException e) {
            errors.add("Vehicle not found with ID: " + dto.getVehicleId());
        }

        try {
            serviceCenter = serviceCenterRepository.findById(dto.getServiceCenterId())
                    .orElseThrow(() -> new EntityNotFoundException("Service center not found"));
        } catch (EntityNotFoundException e) {
            errors.add("Service center not found with ID: " + dto.getServiceCenterId());
        }

        // 2.5. Validate payment method
        List<String> validMethods = Arrays.asList("CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "E_WALLET");
        if (!validMethods.contains(dto.getPaymentMethod().toUpperCase()))
        {
            errors.add("Invalid payment method. Allowed values: " + String.join(", ", validMethods));
        }

        // 3. Check vehicle belongs to customer
        if (customer != null && vehicle != null)
        {
            if (!vehicle.getCustomer().getCustomerID().equals(customer.getCustomerID()))
            {
                errors.add("Vehicle does not belong to this customer");
            }
        }

        // 4. Check time slot availability
        if (serviceCenter != null)
        {
            int currentBookings = ordersRepository.countBookingsInTimeSlot(
                    dto.getServiceCenterId(),
                    dto.getAppointmentDate(),
                    dto.getAppointmentTime());

            if (currentBookings >= MAX_BOOKINGS_PER_SLOT)
            {
                errors.add("Time slot is fully booked. Please choose another time.");
            }
        }

        // 5. Validate appointment time is within working hours
        if (dto.getAppointmentTime().isBefore(OPENING_TIME) ||
            dto.getAppointmentTime().isAfter(CLOSING_TIME.minusHours(1)))
        {
            errors.add("Appointment time must be between " +
                    OPENING_TIME + " and " + CLOSING_TIME.minusHours(1));
        }

        // 7. Validate service or package exists
        if (dto.getServiceId() != null)
        {
            service = servicesRepository.findById(dto.getServiceId()).orElse(null);
            if (service == null)
            {
                errors.add("Service not found with ID: " + dto.getServiceId());
            }
        }
        else if (dto.getPackageId() != null)
        {
            servicePackage = servicePackageRepository.findById(dto.getPackageId()).orElse(null);
            if (servicePackage == null)
            {
                errors.add("Service package not found with ID: " + dto.getPackageId());
            }
        }

        if (!errors.isEmpty())
        {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        // 6. Create order using ModelMapper
        Orders order = new Orders();
        modelMapper.map(dto, order); // Map common fields

        // Set relationships manually
        order.setCustomer(customer);
        order.setVehicle(vehicle);
        order.setServiceCenter(serviceCenter);
        order.setStatus("Pending");
        order.setPaymentStatus(false);
        order.setPaymentMethod(dto.getPaymentMethod().toUpperCase());

        String serviceType;
        Double estimatedCost;

        if (service != null)
        {
            // Đặt service đơn lẻ
            order.setService(service);
            serviceType = service.getServiceName();
            estimatedCost = service.getPrice();
        }
        else
        {
            // Đặt package
            order.getServicePackages().add(servicePackage);
            serviceType = servicePackage.getName();
            estimatedCost = servicePackage.getPrice();
        }

        order.setTotalCost(estimatedCost);

        // 7. Save order
        Orders savedOrder = ordersRepository.save(order);

        // 8. Build response using ModelMapper
        BookingResponse response = modelMapper.map(savedOrder, BookingResponse.class);
        response.setServiceCenterName(serviceCenter.getName());
        response.setServiceType(serviceType);
        response.setEstimatedCost(estimatedCost);
        response.setPaymentMethod(savedOrder.getPaymentMethod());
        response.setMessage("Booking created successfully. We will confirm your appointment shortly.");

        return response;
    }

    //Lấy danh sách bookings của customer

    public List<BookingResponse> getCustomerBookings(Long customerId)
    {
        List<Orders> orders = ordersRepository.findByCustomer_CustomerIDOrderByAppointmentDateDesc(customerId);

        return orders.stream().map(order -> {
            BookingResponse dto = modelMapper.map(order, BookingResponse.class);
            dto.setServiceCenterName(order.getServiceCenter().getName());

            if (order.getService() != null)
            {
                dto.setServiceType(order.getService().getServiceName());
            }
            else if (!order.getServicePackages().isEmpty())
            {
                dto.setServiceType(order.getServicePackages().get(0).getName());
            }

            dto.setEstimatedCost(order.getTotalCost());
            return dto;
        }).collect(Collectors.toList());
    }

    //Cancel booking

    @Transactional
    public void cancelBooking(Long orderId, Long customerId)
    {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        // Verify booking belongs to customer
        if (!order.getCustomer().getCustomerID().equals(customerId))
        {
            throw new IllegalArgumentException("You can only cancel your own bookings");
        }

        // Check if can cancel
        if (order.getStatus().equals("Completed") || order.getStatus().equals("Cancelled"))
        {
            throw new IllegalArgumentException("Cannot cancel booking with status: " + order.getStatus());
        }

        order.setStatus("Cancelled");
        ordersRepository.save(order);
    }
}
