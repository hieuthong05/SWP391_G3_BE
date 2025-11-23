package BE.service;

import BE.entity.*;
import BE.repository.*;
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
    private final VehicleRepository vehicleRepository;

    @Autowired
    public PaymentService(PayOS payOS,
                          PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          OrdersRepository ordersRepository,
                          MaintenanceRepository maintenanceRepository,
                          EmailService emailService,
                          VehicleRepository vehicleRepository) {

        this.payOS = payOS;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.ordersRepository = ordersRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.emailService = emailService;
        this.vehicleRepository = vehicleRepository;
    }

    public Map<String, Object> createPaymentLink(Long invoiceId) throws Exception {
        Payment savedPayment = createPendingPayment(invoiceId);

        // 1️⃣ If payment already has a paymentLinkId, reuse it
        if (savedPayment.getPaymentLinkId() != null && !savedPayment.getPaymentLinkId().isEmpty()) {
            Map<String, Object> paymentResponse = new HashMap<>();
            paymentResponse.put("paymentLinkId", savedPayment.getPaymentLinkId());
            paymentResponse.put("checkoutUrl", "https://pay.payos.vn/web/" + savedPayment.getPaymentLinkId() + "/");
            paymentResponse.put("amount", savedPayment.getAmount());
            paymentResponse.put("qrCode", ""); // optional, leave empty or generate separately
            return paymentResponse;
        }

        // 2️⃣ Otherwise, create new link on PayOS
        String description = "Thanh toan hoa don #" + invoiceId;
        String returnUrl = "http://localhost:5173/payment-success";
        String cancelUrl = "http://localhost:5173/payment-cancel";

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(savedPayment.getOrderCode())
                .amount((long) savedPayment.getAmount())
                .description(description)
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .build();

        CreatePaymentLinkResponse response = this.payOS.paymentRequests().create(paymentData);

        // 3️⃣ Save the new paymentLinkId in DB
        updatePaymentWithLinkId(savedPayment.getPaymentID(), response.getPaymentLinkId());

        Map<String, Object> paymentResponse = new HashMap<>();
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

        Vehicle vehicle = order.getVehicle();
        if (vehicle != null) {
            vehicle.setStatus(true);
            vehicleRepository.save(vehicle);
        } else {
            System.err.println("Quan trọng: Cập nhật trạng thái thành công, nhưng không tìm thấy Vehicle liên quan đến Order ID: " + order.getOrderID());
        }

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

        List<Payment> existingPayments = paymentRepository.findByInvoice_InvoiceID(invoiceId);

        Payment pendingPayment = existingPayments.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getPaymentStatus()))
                .findFirst()
                .orElse(null);

        long newOrderCode = System.currentTimeMillis() / 1000L;

        if (pendingPayment != null) {
            if (newOrderCode <= pendingPayment.getOrderCode()) {
                newOrderCode = pendingPayment.getOrderCode() + 1;
            }
            pendingPayment.setOrderCode(newOrderCode);
            pendingPayment.setPaymentDate(LocalDateTime.now());

            return paymentRepository.save(pendingPayment);
        }

        Payment newPayment = new Payment();
        newPayment.setInvoice(invoice);
        newPayment.setAmount(invoice.getTotalAmount());
        newPayment.setPaymentMethod("PayOS");
        newPayment.setPaymentStatus("PENDING");
        newPayment.setOrderCode(newOrderCode);
        newPayment.setPaymentDate(LocalDateTime.now());

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