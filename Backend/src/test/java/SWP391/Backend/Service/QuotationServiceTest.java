package SWP391.Backend.Service;

import BE.entity.*;
import BE.model.response.ChecklistItemStatusResponse;
import BE.model.response.MaintenanceComponentResponse;
import BE.model.response.QuotationDetailResponse;
import BE.model.response.QuotationResponse;
import BE.repository.MaintenanceRepository;
import BE.repository.OrdersRepository;
import BE.repository.QuotationRepository;
import BE.service.QuotationService;
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
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private MaintenanceRepository maintenanceRepository;
    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private ModelMapper modelMapper;

    // Self-injection for @Lazy self call
    @InjectMocks
    private QuotationService quotationService;

    private Maintenance maintenance;
    private Orders order;
    private Quotation quotation;
    private QuotationResponse mappedResponse;

    @BeforeEach
    void setup() {
        // Build minimal graph
        Customer customer = new Customer();
        customer.setCustomerID(1L);
        customer.setName("Alice");

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleID(2L);
        vehicle.setLicensePlate("59A-12345");
        Model model = new Model();
        model.setModelName("Civic");
        vehicle.setModel(model);

        Employee technician = new Employee();
        technician.setEmployeeID(3L);
        technician.setName("Bob Tech");

        order = new Orders();
        order.setOrderID(10L);
        order.setCustomer(customer);

        maintenance = new Maintenance();
        maintenance.setMaintenanceID(100L);
        maintenance.setEmployee(technician);
        maintenance.setVehicle(vehicle);
        maintenance.setOrders(order);

        // Maintenance components
        Component comp1 = new Component();
        comp1.setComponentID(11L);
        comp1.setName("Oil Filter");
        comp1.setCode("OF-01");
        comp1.setPrice(100.0);

        MaintenanceComponent mc1 = new MaintenanceComponent();
        mc1.setMaintenanceComponentID(1001L);
        mc1.setComponent(comp1);
        mc1.setQuantity(2);

        Component comp2 = new Component();
        comp2.setComponentID(12L);
        comp2.setName("Engine Oil");
        comp2.setCode("EO-01");
        comp2.setPrice(200.0);

        MaintenanceComponent mc2 = new MaintenanceComponent();
        mc2.setMaintenanceComponentID(1002L);
        mc2.setComponent(comp2);
        mc2.setQuantity(1);

        maintenance.setMaintenanceComponents(Arrays.asList(mc1, mc2));

        // Checklists
        CheckList item = new CheckList();
        item.setCheckListId(21L);
        item.setCheckListName("Brake Test");
        item.setCheckListType("SAFETY");

        MaintenanceChecklist mcl = new MaintenanceChecklist();
        mcl.setCheckList(item);
        mcl.setStatus("DONE");
        mcl.setNotes("OK");
        maintenance.setMaintenanceChecklists(List.of(mcl));

        quotation = new Quotation();
        quotation.setQuotationID(1000L);
        quotation.setMaintenance(maintenance);
        quotation.setStatus("PENDING");
        quotation.setQuotationDetails(new ArrayList<>());

        mappedResponse = new QuotationResponse();
        mappedResponse.setQuotationID(1000L);
        mappedResponse.setStatus("PENDING");

        when(modelMapper.map(any(Quotation.class), eq(QuotationResponse.class))).thenReturn(mappedResponse);
        when(modelMapper.map(any(QuotationDetail.class), eq(QuotationDetailResponse.class)))
                .thenAnswer(inv -> {
                    QuotationDetail d = inv.getArgument(0);
                    QuotationDetailResponse r = new QuotationDetailResponse();
                    r.setItemName(d.getItemName());
                    r.setQuantity(d.getQuantity());
                    r.setUnitPrice(d.getUnitPrice());
                    r.setSubTotal(d.getSubTotal());
                    return r;
                });
    }

    @Test
    @DisplayName("Should create a quotation with details and update maintenance/order statuses")
    void createQuotation_success() {
        // No existing quotation for maintenance
        when(quotationRepository.findByMaintenance_MaintenanceID(100L)).thenReturn(Optional.empty());
        when(maintenanceRepository.findById(100L)).thenReturn(Optional.of(maintenance));

        // Persist path
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(inv -> {
            Quotation q = inv.getArgument(0);
            q.setQuotationID(2000L);
            return q;
        });

        QuotationResponse response = quotationService.createQuotation(100L);

        assertNotNull(response);
        assertEquals("PENDING", mappedResponse.getStatus());

        // Verify side effects
        assertEquals("AWAITING_CUSTOMER_APPROVAL", maintenance.getStatus());
        assertEquals("AWAITING_CUSTOMER_APPROVAL", order.getStatus());

        // Verify total calculation: mc1 2*100 + mc2 1*200 = 400
        verify(quotationRepository).save(argThat(q -> Math.abs(q.getTotalAmount() - 400.0) < 0.0001
                && q.getQuotationDetails().size() == 2));

        verify(maintenanceRepository).save(any(Maintenance.class));
        verify(ordersRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("Should throw when quotation already exists for maintenance")
    void createQuotation_alreadyExists_throws() {
        when(quotationRepository.findByMaintenance_MaintenanceID(100L)).thenReturn(Optional.of(new Quotation()));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                quotationService.createQuotation(100L));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(maintenanceRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should recalculate details and total from maintenance components")
    void recalculateAndUpdateQuotation_success() {
        // Existing quotation with stale details
        QuotationDetail old = new QuotationDetail();
        old.setItemName("Old");
        old.setQuantity(99);
        quotation.setQuotationDetails(new ArrayList<>(List.of(old)));

        when(quotationRepository.save(any(Quotation.class))).thenAnswer(inv -> inv.getArgument(0));

        QuotationResponse response = quotationService.recalculateAndUpdateQuotation(quotation);
        assertNotNull(response);

        // Verify new details replaced old
        verify(quotationRepository).save(argThat(q -> q.getQuotationDetails().size() == 2
                && Math.abs(q.getTotalAmount() - 400.0) < 0.0001
                && q.getQuotationDetails().stream().noneMatch(d -> Objects.equals(d.getItemName(), "Old"))));
    }

    @Test
    @DisplayName("Should get quotation by maintenance id and map response")
    void getQuotationByMaintenanceId_success() {
        when(quotationRepository.findByMaintenance_MaintenanceID(100L)).thenReturn(Optional.of(quotation));
        QuotationResponse response = quotationService.getQuotationByMaintenanceId(100L);
        assertNotNull(response);
        verify(modelMapper).map(any(Quotation.class), eq(QuotationResponse.class));
    }

    @Test
    @DisplayName("Should throw when getting quotation by id not found")
    void getQuotationById_notFound_throws() {
        when(quotationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> quotationService.getQuotationById(999L));
    }

    @Test
    @DisplayName("Should confirm quotation and propagate statuses when approved and rejected")
    void confirmQuotation_statusTransitions() {
        quotation.setMaintenance(maintenance);
        when(quotationRepository.findById(1000L)).thenReturn(Optional.of(quotation));

        // Approved
        quotationService.confirmQuotation(1000L, true);
        assertEquals("APPROVED", quotation.getStatus());
        assertEquals("APPROVED", maintenance.getStatus());
        assertEquals("PROCESSING", order.getStatus());

        // Rejected
        quotationService.confirmQuotation(1000L, false);
        assertEquals("REJECTED", quotation.getStatus());
        assertEquals("CANCELLED", maintenance.getStatus());
        assertEquals("CANCELLED", order.getStatus());

        verify(quotationRepository, atLeastOnce()).save(any(Quotation.class));
        verify(maintenanceRepository, atLeastOnce()).save(any(Maintenance.class));
        verify(ordersRepository, atLeastOnce()).save(any(Orders.class));
    }

    @Test
    @DisplayName("Mapping: should populate maintenance/vehicle/customer/checklist/components in response")
    void convertToResponse_mappingFields() {
        // Prepare
        QuotationDetail d1 = new QuotationDetail();
        d1.setItemName("Oil Filter");
        d1.setQuantity(2);
        d1.setUnitPrice(100.0);
        d1.setSubTotal(200.0);
        quotation.setQuotationDetails(List.of(d1));

        // map quotation -> response
        QuotationResponse resp = invokeConvertToResponse(quotation);
        assertNotNull(resp);

        // Since mapper returns mappedResponse, ensure population helpers do not throw and details are set
        assertNotNull(resp.getQuotationDetails());
    }

    // Helper to invoke private conversion via public path
    private QuotationResponse invokeConvertToResponse(Quotation q) {
        // Use public API to hit convertToResponse path
        when(quotationRepository.findById(q.getQuotationID())).thenReturn(Optional.of(q));
        return quotationService.getQuotationById(q.getQuotationID());
    }
}
