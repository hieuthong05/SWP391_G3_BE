package SWP391.Backend.Service;

import BE.entity.*;
import BE.model.request.ConfirmBookingRequest;
import BE.model.response.ConfirmBookingResponse;
import BE.model.response.MaintenanceResponse;
import BE.repository.ComponentRepository;
import BE.repository.EmployeeRepository;
import BE.repository.MaintenanceRepository;
import BE.repository.OrdersRepository;
import BE.service.MaintenanceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MaintenanceServiceTest {

    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private MaintenanceRepository maintenanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    private Orders order;
    private Employee technician;
    private Vehicle vehicle;
    private ServiceCenter serviceCenter;
    private Customer customer;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setCustomerID(1L);
        customer.setName("Alice");
        customer.setPhone("0901");

        Model model = new Model();
        model.setModelID(10L);
        model.setModelName("Civic");

        vehicle = new Vehicle();
        vehicle.setVehicleID(2L);
        vehicle.setLicensePlate("59A-12345");
        vehicle.setModel(model);
        vehicle.setCustomer(customer);

        serviceCenter = new ServiceCenter();
        serviceCenter.setServiceCenterID(3L);
        serviceCenter.setName("Center A");

        BE.entity.Service s1 = new BE.entity.Service();
        s1.setServiceID(11L);
        s1.setServiceName("Oil Change");
        s1.setPrice(100.0);

        BE.entity.Service s2 = new BE.entity.Service();
        s2.setServiceID(12L);
        s2.setServiceName("Tire Rotation");
        s2.setPrice(200.0);

        order = new Orders();
        order.setOrderID(100L);
        order.setCustomer(customer);
        order.setVehicle(vehicle);
        order.setServiceCenter(serviceCenter);
        order.setServices(List.of(s1, s2));
        order.setAppointmentDate(LocalDate.now().plusDays(1));
        order.setAppointmentTime(LocalTime.of(9, 0));
        order.setStatus("Pending");
        order.setTotalCost(300.0);
        order.setNotes("N1");

        technician = new Employee();
        technician.setEmployeeID(5L);
        technician.setName("Bob");
        technician.setStatus(true);
    }

    @Test
    @DisplayName("Should confirm booking and create maintenance with derived description when request description is null")
    void confirmBooking_success_withDerivedDescription() {
        ConfirmBookingRequest req = new ConfirmBookingRequest();
        req.setOrderId(100L);
        req.setTechnicianId(5L);
        req.setMaintenanceDescription(null); // force derived description
        req.setStaffNotes("");

        when(ordersRepository.findByIdWithAllDetails(100L)).thenReturn(Optional.of(order));
        when(maintenanceRepository.existsByOrders_OrderID(100L)).thenReturn(false);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(technician));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> {
            Maintenance m = inv.getArgument(0);
            m.setMaintenanceID(2000L);
            return m;
        });

        ConfirmBookingResponse resp = maintenanceService.confirmBookingAndCreateMaintenance(req);
        assertNotNull(resp);
        assertEquals(100L, resp.getOrderId());
        assertEquals("Confirmed", resp.getOrderStatus());
        assertEquals(2000L, resp.getMaintenanceId());
        assertEquals("Bob", resp.getEmployeeName());
        assertEquals(300.0, resp.getTotalCost());
        assertTrue(resp.getServiceType().contains("Oil Change"));
        assertTrue(resp.getServiceType().contains("Tire Rotation"));

        verify(ordersRepository).save(any(Orders.class));
        verify(maintenanceRepository).save(any(Maintenance.class));
    }

    @Test
    @DisplayName("Should accumulate validation errors when order not found, technician missing, duplicate maintenance, and wrong status")
    void confirmBooking_validationErrors_aggregated() {
        ConfirmBookingRequest req = new ConfirmBookingRequest();
        req.setOrderId(999L);
        req.setTechnicianId(404L);

        when(ordersRepository.findByIdWithAllDetails(999L)).thenReturn(Optional.empty());
        when(maintenanceRepository.existsByOrders_OrderID(999L)).thenReturn(true);
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> maintenanceService.confirmBookingAndCreateMaintenance(req));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Order not found"));
        assertTrue(msg.contains("already has a maintenance"));
        assertTrue(msg.contains("Technician not found"));
    }

    @Test
    @DisplayName("Should reject when order status is not Pending or employee inactive")
    void confirmBooking_invalidStatusOrEmployeeInactive() {
        order.setStatus("Processing");
        technician.setStatus(false);
        ConfirmBookingRequest req = new ConfirmBookingRequest();
        req.setOrderId(100L);
        req.setTechnicianId(5L);

        when(ordersRepository.findByIdWithAllDetails(100L)).thenReturn(Optional.of(order));
        when(maintenanceRepository.existsByOrders_OrderID(100L)).thenReturn(false);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(technician));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> maintenanceService.confirmBookingAndCreateMaintenance(req));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Only orders with 'Pending' status"));
        assertTrue(msg.contains("Employee is not active"));
    }

    @Test
    @DisplayName("Should map maintenance to response for get by id and list methods")
    void getMaintenance_mappings() {
        Maintenance m = buildMaintenance(2000L, "Confirmed");
        when(maintenanceRepository.findById(2000L)).thenReturn(Optional.of(m));

        MaintenanceResponse byId = maintenanceService.getMaintenanceById(2000L);
        assertEquals(2000L, byId.getMaintenanceID());
        assertEquals("Alice", byId.getCustomerName());
        assertEquals("59A-12345", byId.getLicensePlate());
        assertEquals("Civic", byId.getModel());
        assertEquals("Bob", byId.getEmpName());

        when(maintenanceRepository.findAll()).thenReturn(List.of(m));
        assertEquals(1, maintenanceService.getAllMaintenances().size());

        when(employeeRepository.findById(5L)).thenReturn(Optional.of(technician));
        when(maintenanceRepository.findByEmployeeId(5L)).thenReturn(List.of(m));
        assertEquals(1, maintenanceService.getMaintenancesByTechnicianId(5L).size());
    }

    @Test
    @DisplayName("Should throw when maintenance not found for get by id and by order id")
    void getMaintenance_notFound_throws() {
        when(maintenanceRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> maintenanceService.getMaintenanceById(1L));

        when(maintenanceRepository.findByOrderId(9L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> maintenanceService.getMaintenanceByOrderId(9L));
    }

    @Test
    @DisplayName("Should set In Progress and propagate to order")
    void setInProgress_updatesOrderStatus() {
        Maintenance m = buildMaintenance(1L, "Confirmed");
        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(m));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        maintenanceService.setInProgress(1L);
        assertEquals("In Progress", m.getStatus());
        assertEquals("In Progress", m.getOrders().getStatus());
        verify(ordersRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("Should set Waiting For Payment and propagate to order")
    void setWaitingForPayment_updatesOrderStatus() {
        Maintenance m = buildMaintenance(1L, "In Progress");
        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(m));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        maintenanceService.setWaitingForPayment(1L);
        assertEquals("Waiting For Payment", m.getStatus());
        assertEquals("Waiting For Payment", m.getOrders().getStatus());
        verify(ordersRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("Should decrement component stock when completing maintenance; throw if insufficient stock")
    void setCompleted_updatesStocks_andStatuses() {
        // Build components
        Component c1 = new Component();
        c1.setComponentID(10L);
        c1.setName("Oil Filter");
        c1.setQuantity(5);

        MaintenanceComponent mc1 = new MaintenanceComponent();
        mc1.setMaintenanceComponentID(1001L);
        mc1.setComponent(c1);
        mc1.setQuantity(2);

        Maintenance m = buildMaintenance(1L, "In Progress");
        m.setMaintenanceComponents(List.of(mc1));

        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(m));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        maintenanceService.setCompleted(1L);
        assertEquals(3, c1.getQuantity());
        assertEquals("Completed", m.getStatus());
        assertNotNull(m.getEndTime());
        assertEquals("Completed", m.getOrders().getStatus());
        verify(componentRepository).save(c1);
    }

    @Test
    @DisplayName("Should throw when completing maintenance with insufficient stock")
    void setCompleted_throwsWhenInsufficientStock() {
        Component c = new Component();
        c.setComponentID(10L);
        c.setName("Brake Pad");
        c.setQuantity(1);

        MaintenanceComponent mc = new MaintenanceComponent();
        mc.setMaintenanceComponentID(1001L);
        mc.setComponent(c);
        mc.setQuantity(2);

        Maintenance m = buildMaintenance(1L, "In Progress");
        m.setMaintenanceComponents(List.of(mc));

        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(m));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> maintenanceService.setCompleted(1L));
        assertTrue(ex.getMessage().contains("Không đủ số lượng tồn kho"));
        verify(componentRepository, never()).save(any(Component.class));
    }

    @Test
    @DisplayName("Should throw when setInProgress/setWaitingForPayment target not found")
    void statusTransitions_notFound_throws() {
        when(maintenanceRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> maintenanceService.setInProgress(99L));
        assertThrows(EntityNotFoundException.class, () -> maintenanceService.setWaitingForPayment(99L));
    }

    private Maintenance buildMaintenance(Long id, String status) {
        Maintenance m = new Maintenance();
        m.setMaintenanceID(id);
        m.setStatus(status);
        m.setOrders(order);
        m.setVehicle(vehicle);
        m.setEmployee(technician);
        m.setStartTime(LocalDateTime.now());
        return m;
    }
}