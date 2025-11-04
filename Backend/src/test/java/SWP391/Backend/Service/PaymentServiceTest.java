package SWP391.Backend.Service;

import BE.entity.*;
import BE.repository.InvoiceRepository;
import BE.repository.MaintenanceRepository;
import BE.repository.OrdersRepository;
import BE.repository.PaymentRepository;
import BE.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock
    private PayOS payOS;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private MaintenanceRepository maintenanceRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Invoice invoice;
    private Payment savedPayment;
    private Maintenance maintenance;
    private Orders orders;

    @BeforeEach
    void setup() {
        maintenance = new Maintenance();
        maintenance.setMaintenanceID(11L);
        maintenance.setStatus("In Progress");

        orders = new Orders();
        orders.setOrderID(22L);
        orders.setStatus("Pending");
        orders.setPaymentStatus(false);
        maintenance.setOrders(orders);

        invoice = new Invoice();
        invoice.setInvoiceID(33L);
        invoice.setStatus("UNPAID");
        invoice.setTotalAmount(1500000.0);
        invoice.setMaintenance(maintenance);

        InvoiceDetail d1 = new InvoiceDetail();
        d1.setItemName("Oil Change");
        d1.setQuantity(1);
        d1.setUnitPrice(500000.0);
        InvoiceDetail d2 = new InvoiceDetail();
        d2.setItemName("Tire Rotation");
        d2.setQuantity(2);
        d2.setUnitPrice(500000.0);
        invoice.setInvoiceDetails(Arrays.asList(d1, d2));

        savedPayment = new Payment();
        savedPayment.setPaymentID(444L);
        savedPayment.setInvoice(invoice);
        savedPayment.setAmount(invoice.getTotalAmount());
        savedPayment.setPaymentMethod("PayOS");
        savedPayment.setPaymentStatus("PENDING");
    }

    @Test
    @DisplayName("createPaymentLink: builds PaymentData and persists payment link info")
    void testCreatePaymentLink_Success() throws Exception {
        when(invoiceRepository.findById(33L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            // simulate generated id used as order code
            if (p.getPaymentID() == 0) {
                p.setPaymentID(savedPayment.getPaymentID());
            }
            return p;
        });

        CheckoutResponseData response = CheckoutResponseData.builder()
                .paymentLinkId("plink_123")
                .qrCode("qr-data")
                .amount(1500000)
                .description("Thanh toán hóa đơn #33")
                .accountNumber("123456789")
                .accountName("PAYOS")
                .checkoutUrl("https://payos/checkout/plink_123")
                .build();

        when(payOS.createPaymentLink(any(PaymentData.class))).thenReturn(response);

        Map<String, Object> res = paymentService.createPaymentLink(33L);

        assertNotNull(res);
        assertEquals("qr-data", res.get("qrCode"));
        assertEquals(1500000, res.get("amount"));
        assertEquals("plink_123", res.get("paymentLinkId"));
        assertEquals("https://payos/checkout/plink_123", res.get("checkoutUrl"));

        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
        verify(payOS, times(1)).createPaymentLink(any(PaymentData.class));
        // Ensure repository saved with link id
        verify(paymentRepository, atLeastOnce()).save(argThat(p -> "plink_123".equals(p.getPaymentLinkId())));
    }

    @Test
    @DisplayName("createPaymentLink: throws when invoice not found")
    void testCreatePaymentLink_InvoiceNotFound() throws Exception {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.createPaymentLink(999L));
        assertTrue(ex.getMessage().contains("Không tìm thấy hóa đơn"));
        verify(payOS, never()).createPaymentLink(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPaymentLink: throws when PayOS returns null response")
    void testCreatePaymentLink_NullResponse() {
        when(invoiceRepository.findById(33L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        try {
            when(payOS.createPaymentLink(any(PaymentData.class))).thenReturn(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Exception ex = assertThrows(Exception.class, () -> paymentService.createPaymentLink(33L));
        assertTrue(ex.getMessage().contains("response trả về null"));
        // ensure payment not updated with link
        verify(paymentRepository, never()).save(argThat(p -> p.getPaymentLinkId() != null));
    }

    @Test
    @DisplayName("updatePaymentStatusAfterSuccess: happy path updates payment, invoice, maintenance, and order")
    void testUpdatePaymentStatusAfterSuccess_HappyPath() {
        Payment payment = new Payment();
        payment.setPaymentID(1L);
        payment.setPaymentStatus("PENDING");
        payment.setInvoice(invoice);
        payment.setPaymentLinkId("plink_abc");

        when(paymentRepository.findByPaymentLinkId("plink_abc")).thenReturn(Optional.of(payment));

        String msg = paymentService.updatePaymentStatusAfterSuccess("plink_abc");

        assertTrue(msg.contains("Cập nhật trạng thái thanh toán thành công"));
        assertEquals("PAID", payment.getPaymentStatus());
        assertEquals("PAID", invoice.getStatus());
        assertEquals("Completed", maintenance.getStatus());
        assertTrue(orders.getPaymentStatus());
        assertEquals("Completed", orders.getStatus());

        verify(paymentRepository).save(payment);
        verify(invoiceRepository).save(invoice);
        verify(maintenanceRepository).save(maintenance);
        verify(ordersRepository).save(orders);
    }

    @Test
    @DisplayName("updatePaymentStatusAfterSuccess: returns message when already processed")
    void testUpdatePaymentStatusAfterSuccess_AlreadyProcessed() {
        Payment payment = new Payment();
        payment.setPaymentStatus("PAID");
        payment.setPaymentLinkId("plink_done");

        when(paymentRepository.findByPaymentLinkId("plink_done")).thenReturn(Optional.of(payment));

        String msg = paymentService.updatePaymentStatusAfterSuccess("plink_done");
        assertTrue(msg.contains("đã được xử lý trước đó"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePaymentStatusAfterSuccess: missing associations short-circuits with messages")
    void testUpdatePaymentStatusAfterSuccess_MissingAssociations() {
        // payment -> invoice null
        Payment p1 = new Payment();
        p1.setPaymentStatus("PENDING");
        p1.setPaymentLinkId("plink1");
        when(paymentRepository.findByPaymentLinkId("plink1")).thenReturn(Optional.of(p1));
        String m1 = paymentService.updatePaymentStatusAfterSuccess("plink1");
        assertTrue(m1.contains("không tìm thấy Invoice"));

        // payment -> invoice exists but maintenance null
        Payment p2 = new Payment();
        p2.setPaymentStatus("PENDING");
        p2.setPaymentLinkId("plink2");
        Invoice inv2 = new Invoice();
        p2.setInvoice(inv2);
        when(paymentRepository.findByPaymentLinkId("plink2")).thenReturn(Optional.of(p2));
        String m2 = paymentService.updatePaymentStatusAfterSuccess("plink2");
        assertTrue(m2.contains("không tìm thấy Maintenance"));

        // payment -> invoice -> maintenance exists but order null
        Payment p3 = new Payment();
        p3.setPaymentStatus("PENDING");
        p3.setPaymentLinkId("plink3");
        Invoice inv3 = new Invoice();
        Maintenance mai3 = new Maintenance();
        inv3.setMaintenance(mai3);
        p3.setInvoice(inv3);
        when(paymentRepository.findByPaymentLinkId("plink3")).thenReturn(Optional.of(p3));
        String m3 = paymentService.updatePaymentStatusAfterSuccess("plink3");
        assertTrue(m3.contains("không tìm thấy Order"));
    }
}
