package BE.service;

import BE.entity.*;
import BE.model.DTO.AvailableTimeSlotsDTO;
import BE.model.DTO.TimeSlotDTO;
import BE.model.request.BookingRequest;
import BE.model.response.BookingResponse;
import BE.model.response.CustomerBookingResponse;
import BE.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final OrdersRepository ordersRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final ServiceRepository serviceRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final ModelMapper modelMapper;

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
//        if ((dto.getServiceId() == null && dto.getPackageId() == null) ||
//             (dto.getServiceId() != null && dto.getPackageId() != null))
//        {
//            errors.add("Please select either a service or a package, not both");
//        }
        if (dto.getServiceIds() == null || dto.getServiceIds().isEmpty())
        {
            errors.add("Please select at least 1 service.");
        }

        // 2. Validate entities exist

        Customer customer = customerRepository.findById(dto.getCustomerId()).orElse(null);
        if (customer == null)
        {
            errors.add("Customer not found with ID: " + dto.getCustomerId());
        }

        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId()).orElse(null);
        if (vehicle == null)
        {
            errors.add("Vehicle not found with ID: " + dto.getVehicleId());
        }

        ServiceCenter serviceCenter = serviceCenterRepository.findById(dto.getServiceCenterId()).orElse(null);
        if (serviceCenter == null)
        {
            errors.add("Service center not found with ID: " + dto.getServiceCenterId());
        }

        // 6. Validate services //or package exists
        List<BE.entity.Service> services = new ArrayList<>();
//        ServicePackage servicePackage = null;

        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty())
        {
            for (Long serviceId : dto.getServiceIds())
            {
                BE.entity.Service service = serviceRepository.findById(serviceId).orElse(null);
                if (service == null)
                {
                    errors.add("Service not found with ID: " + serviceId);
                }
                else
                {
                    services.add(service);
                }
            }
        }

//        else if (dto.getPackageId() != null)
//        {
//            servicePackage = servicePackageRepository.findById(dto.getPackageId()).orElse(null);
//            if (servicePackage == null)
//            {
//                errors.add("Service package not found with ID: " + dto.getPackageId());
//            }
//        }

        // 3. Check vehicle belongs to customer
        if (customer != null && vehicle != null)
        {
            if (!vehicle.getCustomer().getCustomerID().equals(customer.getCustomerID()))
            {
                errors.add("Vehicle does not belong to this customer");
            }
        }

//        // 2.5. Validate payment method
//        List<String> validMethods = Arrays.asList("CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "E_WALLET");
//        if (!validMethods.contains(dto.getPaymentMethod().toUpperCase()))
//        {
//            errors.add("Invalid payment method. Allowed values: " + String.join(", ", validMethods));
//        }


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


        if (!errors.isEmpty())
        {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        // 7. Create order
        Orders order = new Orders();
        modelMapper.map(dto, order); // Map common fields

        // Set relationships manually
        order.setCustomer(customer);
        order.setVehicle(vehicle);
        order.setServiceCenter(serviceCenter);
        order.setStatus("Pending");
        order.setPaymentStatus(false);
        order.setPaymentMethod(dto.getPaymentMethod().toUpperCase());


        // 11. Calculate total cost and set services
        Double totalCost = 0.0;
        StringBuilder serviceTypes = new StringBuilder();

        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty())
        {
            // Thêm tất cả services vào order
            order.getServices().addAll(services);

            for (BE.entity.Service service : services)
            {
                totalCost += service.getPrice();
                if (serviceTypes.length() > 0) serviceTypes.append(", ");
                serviceTypes.append(service.getServiceName());
            }
        }

        order.setTotalCost(totalCost);


//        else
//        {
//            order.getServicePackages().add(servicePackage);
//            serviceType = servicePackage.getName();
//            estimatedCost = servicePackage.getPrice();
//        }

//        order.setTotalCost(estimatedCost);

        // 8. Save order
        Orders savedOrder = ordersRepository.save(order);

        // 9. Build response using ModelMapper
        BookingResponse response = modelMapper.map(savedOrder, BookingResponse.class);
        response.setServiceCenterName(serviceCenter.getName());
        response.setServiceType(serviceTypes.toString());
        response.setTotalCost(totalCost);
        response.setPaymentMethod(savedOrder.getPaymentMethod());
        response.setMessage("Booking created successfully with " +
                            services.size() +
                            " service(s). We will confirm your appointment shortly.");

        return response;
    }

    //Lấy danh sách bookings của customer

    // Lấy danh sách bookings của customer
//    public List<BookingResponse> getCustomerBookings(Long customerId) {
//        List<Orders> orders = ordersRepository.findByCustomer_CustomerIDOrderByAppointmentDateDesc(customerId);
//
//        return orders.stream().map(order -> {
//            BookingResponse dto = modelMapper.map(order, BookingResponse.class);
//            dto.setServiceCenterName(order.getServiceCenter().getName());
//
//            // Xử lý services (ManyToMany)
//            StringBuilder serviceType = new StringBuilder();
//            Double totalCost = 0.0;
//
//            if (!order.getServices().isEmpty()) {
//                // Nếu đặt services
//                for (int i = 0; i < order.getServices().size(); i++) {
//                    Service service = order.getServices().get(i);
//                    if (i > 0) serviceType.append(", ");
//                    serviceType.append(service.getServiceName());
//                    totalCost += service.getPrice();
//                }
//                dto.setServiceType(serviceType.toString());
//                dto.setBookingType("SERVICE"); // Thêm field này nếu cần phân biệt
//            }
//            else if (!order.getServicePackages().isEmpty()) {
//                // Nếu đặt packages
//                for (int i = 0; i < order.getServicePackages().size(); i++) {
//                    ServicePackage pkg = order.getServicePackages().get(i);
//                    if (i > 0) serviceType.append(", ");
//                    serviceType.append(pkg.getName());
//                    totalCost += pkg.getPrice();
//                }
//                dto.setServiceType(serviceType.toString());
//                dto.setBookingType("PACKAGE");
//            }
//
//            dto.setEstimatedCost(order.getTotalCost());
//            return dto;
//        }).collect(Collectors.toList());
//    }

    //Cancel booking

    @Transactional
    public void cancelBooking(Long orderId, Long requestUserId, String role) {
        // Tìm đơn hàng
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + orderId));

        // Validate quyền sở hữu (Chỉ áp dụng cho Customer)
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            if (!order.getCustomer().getCustomerID().equals(requestUserId)) {
                throw new IllegalArgumentException("You can only cancel your own bookings");
            }
        }

        // Kiểm tra trạng thái đơn hàng
        // Chặn hủy nếu đơn đã: Hoàn thành, Đã hủy, hoặc Đang thực hiện
        String currentStatus = order.getStatus();
        if ("Completed".equalsIgnoreCase(currentStatus) ||
                "Cancelled".equalsIgnoreCase(currentStatus) ||
                "In Progress".equalsIgnoreCase(currentStatus))
        {
            throw new IllegalArgumentException("Cannot cancel booking with status: " + currentStatus);
        }

        if ("Confirmed".equalsIgnoreCase(currentStatus)) {
            Vehicle vehicle = order.getVehicle();
            if (vehicle != null) {
                vehicle.setStatus(true); // Set vehicle back to Available
                vehicleRepository.save(vehicle);
            }
        }

        // Cập nhật trạng thái sang Cancelled
        order.setStatus("Cancelled");

        // Tự động ghi chú nếu người hủy là Admin/Staff
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            String oldNotes = order.getNotes() == null ? "" : order.getNotes();
            String autoNote = " | Cancelled by " + role.toUpperCase() + " (No-Show/Request)";
            order.setNotes(oldNotes + autoNote);
        }
        ordersRepository.save(order);
    }

    @Transactional
    public List<BookingResponse> getAllBookings()
    {
        List<Orders> orders = ordersRepository.findAllWithDetailsAndTechnician();

        return orders.stream().map(order -> {
            BookingResponse response = new BookingResponse();

            response.setOrderId(order.getOrderID());
            response.setStatus(order.getStatus());
            response.setAppointmentDate(order.getAppointmentDate());
            response.setAppointmentTime(order.getAppointmentTime());
            response.setOrderDate(order.getOrderDate());
            response.setTotalCost(order.getTotalCost());
            response.setPaymentMethod(order.getPaymentMethod());
            response.setPaymentStatus(order.getPaymentStatus());
            response.setNotes(order.getNotes());

            //Service Center
            if (order.getServiceCenter() != null)
            {
                response.setServiceCenterId(order.getServiceCenter().getServiceCenterID());
                response.setServiceCenterName(order.getServiceCenter().getName());
            }

            //Customer
            if (order.getCustomer() != null)
            {
                response.setCustomerId(order.getCustomer().getCustomerID());
                response.setCustomerName(order.getCustomer().getName());
                response.setCustomerPhone(order.getCustomer().getPhone());
                response.setCustomerEmail(order.getCustomer().getEmail());
            }

            //Vehicle
            if (order.getVehicle() != null)
            {
                response.setVehicleId(order.getVehicle().getVehicleID());
                response.setVehiclePlateNumber(order.getVehicle().getLicensePlate());
                response.setVehicleModel(order.getVehicle().getModel().getModelName());
            }

            //Services
            if (order.getServices() != null && !order.getServices().isEmpty())
            {
                List<String> serviceNames = order.getServices().stream()
                        .map(BE.entity.Service::getServiceName)
                        .collect(Collectors.toList());

                response.setServiceNames(serviceNames);
                response.setServiceType(String.join(", ", serviceNames));
            }

            setTechnicianDetails(order, response);

            return response;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<BookingResponse> getBookingsByServiceCenter(Long serviceCenterId)
    {
        // Lấy tất cả orders theo serviceCenterId
        List<Orders> orders = ordersRepository.findByServiceCenter_ServiceCenterID(serviceCenterId);

        // Map từng order sang BookingResponse
        return orders.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookingResponse> getBookingsByStatus(String status)
    {
        if (status == null || status.trim().isEmpty())
        {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        List<String> validStatuses = Arrays.asList("Pending", "Confirmed", "In Progress", "Waiting For Payment", "Completed", "Cancelled");
        if (!validStatuses.contains(status))
        {
            throw new IllegalArgumentException("Invalid status. Valid values are: " + String.join(", ", validStatuses));
        }

        List<Orders> orders = ordersRepository.findByStatusWithDetails(status);

        if (orders.isEmpty())
        {
            return new ArrayList<>();
        }

        return orders.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    //Booking Response cho Staff/Technician
    private BookingResponse mapToBookingResponse(Orders order)
    {
        BookingResponse response = new BookingResponse();

        // Thông tin cơ bản từ Orders
        response.setOrderId(order.getOrderID());
        response.setStatus(order.getStatus());
        response.setAppointmentDate(order.getAppointmentDate());
        response.setAppointmentTime(order.getAppointmentTime());
        response.setOrderDate(order.getOrderDate());
        response.setTotalCost(order.getTotalCost());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setNotes(order.getNotes());

        // Service Center info
        if (order.getServiceCenter() != null)
        {
            response.setServiceCenterId(order.getServiceCenter().getServiceCenterID());
            response.setServiceCenterName(order.getServiceCenter().getName());
        }

        // Services info
        if (order.getServices() != null && !order.getServices().isEmpty())
        {
            List<String> serviceNames = order.getServices().stream()
                    .map(BE.entity.Service::getServiceName)
                    .collect(Collectors.toList());

            response.setServiceNames(serviceNames);
            response.setServiceType(String.join(", ", serviceNames));
        }

        // Customer info
        if (order.getCustomer() != null)
        {
            response.setCustomerId(order.getCustomer().getCustomerID());
            response.setCustomerName(order.getCustomer().getName());
            response.setCustomerPhone(order.getCustomer().getPhone());
            response.setCustomerEmail(order.getCustomer().getEmail());
        }

        // Vehicle info
        if (order.getVehicle() != null)
        {
            response.setVehicleId(order.getVehicle().getVehicleID());
            response.setVehiclePlateNumber(order.getVehicle().getLicensePlate());
            response.setVehicleModel(order.getVehicle().getModel().getModelName());
        }

        setTechnicianDetails(order, response);

        return response;
    }

    /**
     * Optional: Lấy bookings theo nhiều status
     */
    public List<BookingResponse> getBookingsByMultipleStatus(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            throw new IllegalArgumentException("Status list cannot be null or empty");
        }

        List<Orders> orders = ordersRepository.findByStatusInWithDetails(statuses);

        return orders.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Optional: Lấy thống kê theo tất cả status
     */
    public Map<String, Long> getBookingStatusStatistics() {
        Map<String, Long> stats = new HashMap<>();

        List<String> statuses = Arrays.asList("Pending", "Confirmed", "In Progress", "Completed", "Cancelled");

        for (String status : statuses) {
            long count = ordersRepository.countByStatus(status);
            stats.put(status, count);
        }

        return stats;
    }

    //--------------------------------------------------------BY CUSTOMER ID-------------------------------------------------------------------------

    @Transactional
    public List<CustomerBookingResponse> getBookingsByCustomerId(Long customerId)
    {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));

        List<Orders> orders = ordersRepository.findByCustomerIdWithDetails(customerId);

        if (orders.isEmpty())
        {
            return new ArrayList<>();
        }

        return orders.stream()
                .map(this::mapToCustomerBookingResponse)
                .collect(Collectors.toList());
    }


    //Booking Response cho Customer
    private CustomerBookingResponse mapToCustomerBookingResponse(Orders order)
    {
        CustomerBookingResponse response = new CustomerBookingResponse();

        response.setOrderId(order.getOrderID());
        response.setStatus(order.getStatus());
        response.setAppointmentDate(order.getAppointmentDate());
        response.setAppointmentTime(order.getAppointmentTime());
        response.setOrderDate(order.getOrderDate());
        response.setTotalCost(order.getTotalCost());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setNotes(order.getNotes());

        //Service Center
        if (order.getServiceCenter() != null)
        {
            response.setServiceCenterName(order.getServiceCenter().getName());
        }

        //Services
        if (order.getServices() != null && !order.getServices().isEmpty())
        {
            List<String> serviceNames = order.getServices().stream()
                    .map(BE.entity.Service::getServiceName)
                    .collect(Collectors.toList());

            response.setServiceNames(serviceNames);
            response.setServiceType(String.join(", ", serviceNames));
        }

        //Vehicle
        if (order.getVehicle() != null)
        {
            response.setVehiclePlateNumber(order.getVehicle().getLicensePlate());
            response.setVehicleModel(order.getVehicle().getModel().getModelName());
        }
        return response;
    }

    private void setTechnicianDetails(Orders order, BookingResponse response) {
        // Tìm kiếm Maintenance liên quan đến Order này
        Maintenance maintenance = maintenanceRepository.findByOrder_OrderID(order.getOrderID());

        if (maintenance != null && maintenance.getEmployee() != null) {
            Employee technician = maintenance.getEmployee();

            response.setTechnicianId(technician.getEmployeeID());
            response.setTechnicianName(technician.getName());
            response.setTechnicianPhone(technician.getPhone());
        }
    }

//    /**
//     * Alternative: Lấy bookings với filter theo status
//     */
//    public List<CustomerBookingResponse> getBookingsByCustomerIdAndStatus(
//            Long customerId, String status) {
//
//        customerRepository.findById(customerId)
//                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));
//
//        List<Orders> orders;
//        if (status == null || status.isEmpty()) {
//            orders = ordersRepository.findByCustomerIdWithDetails(customerId);
//        } else {
//            orders = ordersRepository.findByCustomerIdAndStatus(customerId, status);
//        }
//
//        return orders.stream()
//                .map(this::mapToCustomerBookingResponse)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Lấy upcoming bookings của customer (chưa hoàn thành)
//     */
//    public List<CustomerBookingResponse> getUpcomingBookingsByCustomerId(Long customerId) {
//        customerRepository.findById(customerId)
//                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));
//
//        List<Orders> orders = ordersRepository.findUpcomingBookingsByCustomerId(
//                customerId, LocalDate.now());
//
//        return orders.stream()
//                .map(this::mapToCustomerBookingResponse)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Lấy booking history của customer (đã hoàn thành hoặc đã hủy)
//     */
//    public List<CustomerBookingResponse> getBookingHistoryByCustomerId(Long customerId) {
//        customerRepository.findById(customerId)
//                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));
//
//        List<String> completedStatuses = Arrays.asList("Completed", "Cancelled");
//        List<Orders> orders = ordersRepository.findByCustomerIdAndStatusIn(
//                customerId, completedStatuses);
//
//        return orders.stream()
//                .map(this::mapToCustomerBookingResponse)
//                .collect(Collectors.toList());
//    }
}
