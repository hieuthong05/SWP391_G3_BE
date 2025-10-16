package BE.service;

import BE.entity.Employee;
import BE.entity.Maintenance;
import BE.entity.Orders;
import BE.model.request.ConfirmBookingRequest;
import BE.model.response.ConfirmBookingResponse;
import BE.repository.EmployeeRepository;
import BE.repository.MaintenanceRepository;
import BE.repository.OrdersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public ConfirmBookingResponse confirmBookingAndCreateMaintenance(ConfirmBookingRequest request)
    {
        List<String> errors = new ArrayList<>();

        Orders order = ordersRepository.findByIdWithAllDetails(request.getOrderId()).orElse(null);
        if (order == null)
        {
            errors.add("Order not found with ID: " + request.getOrderId());
        }

        if (!"Pending".equals(order.getStatus()))
        {
            errors.add("Only orders with 'Pending' status can be confirmed. Current status: " + order.getStatus());
        }

        if (maintenanceRepository.existsByOrders_OrderID(request.getOrderId()))
        {
            errors.add("This order already has a maintenance record");
        }

        Employee employee = employeeRepository.findById(request.getTechnicianId()).orElse(null);
        if (employee == null)
        {
            errors.add("Technician not found with ID: " + request.getTechnicianId());
        }

        if (!employee.isStatus())
        {
            errors.add("Employee is not active");
        }

        if (!errors.isEmpty())
        {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        order.setStatus("Confirmed");
        Orders savedOrder = ordersRepository.save(order);

        Maintenance maintenance = new Maintenance();
        maintenance.setOrders(savedOrder);
        maintenance.setEmployee(employee);
        maintenance.setVehicle(savedOrder.getVehicle());


        String servicesDescription = savedOrder.getServices().stream()
                .map(BE.entity.Service::getServiceName)
                .collect(Collectors.joining(", "));

        String description = request.getMaintenanceDescription() != null
                ? request.getMaintenanceDescription()
                : "Maintenance for services: " + servicesDescription;
        maintenance.setDescription(description);

        maintenance.setCost(savedOrder.getTotalCost());
        maintenance.setStartTime(LocalDateTime.now());
        maintenance.setStatus("In Progress");
        maintenance.setNotes(savedOrder.getNotes());

        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        return mapToConfirmBookingResponse(savedMaintenance);
    }

    private ConfirmBookingResponse mapToConfirmBookingResponse(Maintenance maintenance)
    {
        ConfirmBookingResponse response = new ConfirmBookingResponse();

        // Order info
        response.setOrderId(maintenance.getOrders().getOrderID());
        response.setOrderStatus(maintenance.getOrders().getStatus());
        response.setAppointmentDate(maintenance.getOrders().getAppointmentDate());
        response.setAppointmentTime(maintenance.getOrders().getAppointmentTime());

        // Maintenance info
        response.setMaintenanceId(maintenance.getMaintenanceID());
        response.setMaintenanceStatus(maintenance.getStatus());
        response.setStartTime(maintenance.getStartTime());

        if (maintenance.getOrders().getCustomer() != null)
        {
            response.setCustomerName(maintenance.getOrders().getCustomer().getName());
        }

        if (maintenance.getOrders().getVehicle() != null)
        {
            response.setVehiclePlateNumber(maintenance.getVehicle().getLicensePlate());
            response.setVehicleModel(maintenance.getVehicle().getModel().getModelName());
        }

        if (maintenance.getOrders().getServiceCenter() != null)
        {
            response.setServiceCenterName(maintenance.getOrders().getServiceCenter().getName());
        }

        response.setEmployeeId(maintenance.getEmployee().getEmployeeID());
        response.setEmployeeName(maintenance.getEmployee().getName());

        if (maintenance.getOrders().getServices() != null && !maintenance.getOrders().getServices().isEmpty())
        {
            String serviceType = maintenance.getOrders().getServices().stream()
                    .map(BE.entity.Service::getServiceName)
                    .collect(Collectors.joining(", "));
            response.setServiceType(serviceType);
        }

        response.setTotalCost(maintenance.getOrders().getTotalCost());

        response.setMessage("Confirm booking successfully and maintenance record created");
        return response;
    }


     //* Lấy tất cả maintenance records

    public List<Maintenance> getAllMaintenances()
    {
        return maintenanceRepository.findAll();
    }


     //* Lấy maintenance theo order ID

    public Maintenance getMaintenanceByOrderId(Long orderId)
    {
        return maintenanceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Maintenance not found for order ID: " + orderId));
    }
}
