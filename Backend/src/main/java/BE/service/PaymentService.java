package BE.service;

import BE.entity.*;
import BE.repository.InvoiceRepository;
import BE.repository.MaintenanceRepository;
import BE.repository.OrdersRepository;
import BE.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// IMPORT V2 - ĐÃ SỬA
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrdersRepository ordersRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final EmailService emailService;

    @Autowired
    public PaymentService(PayOS payOS,
                          PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          OrdersRepository ordersRepository,
                          MaintenanceRepository maintenanceRepository,
                          EmailService emailService) {

        this.payOS = payOS;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.ordersRepository = ordersRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.emailService = emailService;
    }

    public Map<String, Object> createPaymentLink(Long invoiceId) throws Exception {

        Payment savedPayment = createPendingPayment(invoiceId);

        // Dữ liệu cho PayOS
        String description = "Thanh toan hoa don #" + invoiceId;
        String returnUrl = "http://localhost:5173/payment-success";
        String cancelUrl = "http://localhost:5173/payment-cancel";

        List<PaymentLinkItem> items = new ArrayList<>();
        for (InvoiceDetail detail : savedPayment.getInvoice().getInvoiceDetails()) {
            String itemName = detail.getItemName() != null ? detail.getItemName() : "Hang muc hoa don";

            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(itemName)
                    .quantity(detail.getQuantity())
                    .price((long) detail.getUnitPrice())
                    .build();

            items.add(item);
        }

        // SỬ DỤNG CreatePaymentLinkRequest (v2)
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(savedPayment.getOrderCode()) // <-- ĐÃ XÓA (int)
                .amount((long) savedPayment.getAmount())
                .description(description)
                .items(items)
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .build();

        // GỌI HÀM V2
        CreatePaymentLinkResponse response;
        try {
            // Kiểu trả về của .create() chính là CreatePaymentLinkResponse
            response = this.payOS.paymentRequests().create(paymentData);
        } catch (Exception e) {
            // Nếu PayOS trả về lỗi (4xx, 5xx), nó sẽ ném ra Exception
            throw new Exception("Lỗi khi gọi API PayOS v2: " + e.getMessage(), e);
        }

        if (response == null) {
            throw new Exception("Lỗi khi tạo link thanh toán từ PayOS, response trả về null.");
        }

        // === PHẦN SỬA LỖI CHÍNH ===
        //
        // Bỏ đoạn kiểm tra response.getData() == null
        // vì class CreatePaymentLinkResponse không có .getData()
        // Thay vào đó, ta kiểm tra một trường quan trọng, ví dụ paymentLinkId
        //
        if (response.getPaymentLinkId() == null || response.getPaymentLinkId().isEmpty()) {
            // Chúng ta không còn .getCode() hay .getDesc() ở đây
            // vì nếu có lỗi, nó đã bị bắt ở khối try-catch bên trên.
            throw new Exception("Lỗi PayOS: Dữ liệu trả về không hợp lệ (không có paymentLinkId).");
        }

        // Cập nhật Payment với paymentLinkId (truy cập trực tiếp)
        updatePaymentWithLinkId(savedPayment.getPaymentID(), response.getPaymentLinkId());

        Map<String, Object> paymentResponse = new HashMap<>();

        // Lấy dữ liệu trực tiếp từ response (KHÔNG CÓ .getData())
        paymentResponse.put("qrCode", response.getQrCode());
        paymentResponse.put("amount", response.getAmount());
        paymentResponse.put("paymentLinkId", response.getPaymentLinkId());
        paymentResponse.put("checkoutUrl", response.getCheckoutUrl());

        return paymentResponse;
    }

    @Transactional
    public String updatePaymentStatusAfterSuccess(String paymentLinkId) {
        // Tìm Payment
        Payment payment = paymentRepository.findByPaymentLinkId(paymentLinkId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thanh toán với Link ID: " + paymentLinkId));

        if (!"PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
            return "Trạng thái thanh toán đã được xử lý trước đó: " + payment.getPaymentStatus();
        }
        payment.setPaymentStatus("PAID");
        paymentRepository.save(payment);

        // Cập nhật Invoice
        Invoice invoice = payment.getInvoice();
        if (invoice == null) {
            return "Cập nhật trạng thái Payment thành công, nhưng không tìm thấy Invoice liên quan.";
        }

        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        // Cập nhật Maintenance
        Maintenance maintenance = invoice.getMaintenance();
        if (maintenance == null) {
            return "Cập nhật trạng thái Payment và Invoice thành công, nhưng không tìm thấy Maintenance liên quan.";
        }

        updateMaintenanceStatusOnPayment(maintenance);

        // Cập nhật Order
        Orders order = maintenance.getOrders();
        if (order == null) {
            return "Cập nhật trạng thái Payment và Maintenance thành công, nhưng không tìm thấy Order liên quan.";
        }

        updateOrderStatusOnPayment(order);
        try {
            emailService.sendInvoiceEmail(invoice);
        } catch (Exception e) {
            System.err.println("Quan trọng: Thanh toán thành công (Link ID: " + paymentLinkId + ") nhưng gửi email hóa đơn thất bại. Lỗi: " + e.getMessage());
            e.printStackTrace();
        }

        return "Cập nhật trạng thái thanh toán thành công cho Payment ID: " + payment.getPaymentID() + " (Link ID: " + paymentLinkId + ")";
    }

    private void updateMaintenanceStatusOnPayment(Maintenance maintenance) {
        if (!"Completed".equalsIgnoreCase(maintenance.getStatus()) && !"Cancelled".equalsIgnoreCase(maintenance.getStatus())) {
            maintenance.setStatus("Completed");
            maintenance.setEndTime(LocalDateTime.now());
            maintenanceRepository.save(maintenance);
        }
    }

    private void updateOrderStatusOnPayment(Orders order) {
        if (!"Completed".equalsIgnoreCase(order.getStatus()) && !"Cancelled".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("Completed");
            order.setPaymentStatus(true);
            ordersRepository.save(order);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment createPendingPayment(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        // SỬA LỖI TRÀN SỐ INT (chia 1000 để lấy số giây)
        long orderCode = System.currentTimeMillis() / 1000L;

        Payment newPayment = new Payment();
        newPayment.setInvoice(invoice);
        newPayment.setAmount(invoice.getTotalAmount());
        newPayment.setPaymentMethod("PayOS");
        newPayment.setPaymentStatus("PENDING");
        newPayment.setOrderCode(orderCode); // Lưu số long (nhỏ)

        return paymentRepository.save(newPayment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePaymentWithLinkId(Long paymentId, String paymentLinkId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Payment ID: " + paymentId + " để cập nhật linkId"));

        payment.setPaymentLinkId(paymentLinkId);
        paymentRepository.save(payment);
    }

}