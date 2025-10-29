package BE.service;

import BE.entity.*;
import BE.model.request.ConfirmBookingRequest;
import BE.model.response.BookingResponse;
import BE.model.response.ConfirmBookingResponse;
import BE.model.response.MaintenanceResponse;
import BE.repository.ComponentRepository;
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

    @Autowired
    private ComponentRepository componentRepository;


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
        maintenance.setStatus(savedOrder.getStatus());
        maintenance.setNotes(request.getStaffNotes() != null && !request.getStaffNotes().isEmpty()
                ? request.getStaffNotes()
                : savedOrder.getNotes());

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

    public List<MaintenanceResponse> getAllMaintenances()
    {
        List<Maintenance> maintenances = maintenanceRepository.findAll();

        if (maintenances.isEmpty())
        {
            return new ArrayList<>();
        }

        return maintenances.stream()
            .map(this::mapToMaintenanceResponse)
            .collect(Collectors.toList());
    }


     //* Lấy maintenance theo maintenance ID

    @Transactional(readOnly = true)
    public MaintenanceResponse getMaintenanceById(Long maintenanceId)
    {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Maintenance not found with ID: " + maintenanceId));

        return mapToMaintenanceResponse(maintenance);
    }

    public List<MaintenanceResponse> getMaintenancesByTechnicianId(Long technicianId)
    {
        Employee employee = employeeRepository.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Technician not found with ID: " + technicianId));

        List<Maintenance> maintenances = maintenanceRepository.findByEmployeeId(technicianId);

        if (maintenances.isEmpty())
        {
            return new ArrayList<>();
        }

        return maintenances.stream()
                .map(this::mapToMaintenanceResponse)
                .collect(Collectors.toList());
    }


     // Lấy maintenance theo order ID
    public Maintenance getMaintenanceByOrderId(Long orderId)
    {
        return maintenanceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Maintenance not found for order ID: " + orderId));
    }

    private MaintenanceResponse mapToMaintenanceResponse(Maintenance maintenance)
    {
        MaintenanceResponse response = new MaintenanceResponse();

        response.setMaintenanceID(maintenance.getMaintenanceID());
        if (maintenance.getOrders() != null)
        {
            response.setOrderID(maintenance.getOrders().getOrderID());
            response.setCustomerName(maintenance.getOrders().getCustomer().getName());
            response.setCustomerPhone(maintenance.getOrders().getCustomer().getPhone());
        }

        if (maintenance.getEmployee() != null)
        {
            response.setEmpID(maintenance.getEmployee().getEmployeeID());
            response.setEmpName(maintenance.getEmployee().getName());
        }

        if (maintenance.getVehicle() != null)
        {
            response.setVehicleID(maintenance.getVehicle().getVehicleID());
            response.setLicensePlate(maintenance.getVehicle().getLicensePlate());
            response.setModel(maintenance.getVehicle().getModel().getModelName());
        }

        response.setDescription(maintenance.getDescription());
        response.setCost(maintenance.getCost());

        response.setStartTime(maintenance.getStartTime());
        response.setEndTime(maintenance.getEndTime());
        response.setNextDueDate(maintenance.getNextDueDate());

        response.setStatus(maintenance.getStatus());
        response.setNotes(maintenance.getNotes());

        if (maintenance.getInvoice() != null)
        {
            response.setInvoiceID(maintenance.getInvoice().getInvoiceID());
        }

        return response;
    }

    @Transactional
    public void setInProgress(Long maintenanceID)
    {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceID)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));

        maintenance.setStatus("In Progress");
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);
        savedMaintenance.getOrders().setStatus(savedMaintenance.getStatus());
        ordersRepository.save(savedMaintenance.getOrders());
    }

    @Transactional
    public void setWaitingForPayment(Long maintenanceID)
    {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceID)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));

        maintenance.setStatus("Waiting For Payment");
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);
        savedMaintenance.getOrders().setStatus(savedMaintenance.getStatus());
        ordersRepository.save(savedMaintenance.getOrders());
    }

    @Transactional
    public void setCompleted(Long maintenanceID) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceID)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));

        if (maintenance.getMaintenanceComponents() != null && !maintenance.getMaintenanceComponents().isEmpty()) {
            for (MaintenanceComponent mc : maintenance.getMaintenanceComponents()) {
                Component component = mc.getComponent();
                int quantityUsed = mc.getQuantity();

                if (component == null) {
                    System.err.println("Warning: Component is null for MaintenanceComponent ID: " + mc.getMaintenanceComponentID());
                    continue;
                }

                Integer currentStock = component.getQuantity();
                if (currentStock == null || currentStock < quantityUsed) {
                    throw new IllegalStateException("Không đủ số lượng tồn kho cho linh kiện: "
                            + component.getName() + " (ID: " + component.getComponentID() + "). "
                            + "Yêu cầu: " + quantityUsed + ", Tồn kho: " + (currentStock == null ? 0 : currentStock));
                }

                component.setQuantity(currentStock - quantityUsed);
                componentRepository.save(component);
            }
        }

        maintenance.setStatus("Completed");
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        if (savedMaintenance.getOrders() != null) {
            savedMaintenance.getOrders().setStatus(savedMaintenance.getStatus());
            ordersRepository.save(savedMaintenance.getOrders());
        } else {
            System.err.println("Warning: Order not found for completed Maintenance ID: " + maintenanceID);
        }
    }
}
