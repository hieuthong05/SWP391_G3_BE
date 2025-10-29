package BE.service;

import BE.entity.*;
import BE.repository.InvoiceRepository;
import BE.repository.MaintenanceRepository;
import BE.repository.OrdersRepository;
import BE.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class PaymentService {

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrdersRepository ordersRepository;
    private final ObjectMapper objectMapper;
    private final String payosChecksumKey;
    private final MaintenanceRepository maintenanceRepository;

    @Autowired
    public PaymentService(PayOS payOS,
                          PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          OrdersRepository ordersRepository,
                          ObjectMapper objectMapper,
                          MaintenanceRepository maintenanceRepository,
                          @Value("${payos.checksum-key}") String payosChecksumKey) {

        this.payOS = payOS;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.ordersRepository = ordersRepository;
        this.objectMapper = objectMapper;
        this.maintenanceRepository = maintenanceRepository;
        this.payosChecksumKey = payosChecksumKey;
    }

    @Transactional
    // --- Thay đổi kiểu trả về từ String sang Map<String, Object> ---
    public Map<String, Object> createPaymentLink(Long invoiceId) throws Exception {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        Payment newPayment = new Payment();
        newPayment.setInvoice(invoice);
        newPayment.setAmount(invoice.getTotalAmount());
        newPayment.setPaymentMethod("PayOS");
        newPayment.setPaymentStatus("PENDING");
        Payment savedPayment = paymentRepository.save(newPayment);

        long orderCode = savedPayment.getPaymentID();
        String description = "Thanh toán hóa đơn #" + invoice.getInvoiceID();
        // Return URL vẫn cần thiết để PayOS biết chuyển hướng sau khi thanh toán thành công (qua app ngân hàng chẳng hạn)
        String returnUrl = "http://localhost:5173/payment-success";
        String cancelUrl = "http://localhost:5173/payment-cancel";

        List<ItemData> items = new ArrayList<>();
        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
            String itemName = detail.getItemName() != null ? detail.getItemName() : "Hạng mục hóa đơn"; // Lấy tên thật nếu có

            ItemData item = ItemData.builder()
                    .name(itemName)
                    .quantity(detail.getQuantity())
                    .price((int) detail.getUnitPrice())
                    .build();
            items.add(item);
        }

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount((int) savedPayment.getAmount())
                .description(description)
                .items(items)
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .build();

        CheckoutResponseData response = this.payOS.createPaymentLink(paymentData);

        if (response == null) {
            throw new Exception("Lỗi khi tạo link thanh toán từ PayOS, response trả về null.");
        }

        savedPayment.setPaymentLinkId(response.getPaymentLinkId());
        paymentRepository.save(savedPayment);

        // --- Tạo Map chứa dữ liệu trả về cho Frontend ---
        Map<String, Object> paymentResponse = new HashMap<>();
        paymentResponse.put("qrCode", response.getQrCode()); // URL ảnh QR code
        paymentResponse.put("amount", response.getAmount()); // Số tiền
        paymentResponse.put("description", response.getDescription()); // Mô tả
        paymentResponse.put("accountNumber", response.getAccountNumber()); // Số tài khoản VietQR
        paymentResponse.put("accountName", response.getAccountName()); // Tên tài khoản VietQR
        paymentResponse.put("paymentLinkId", response.getPaymentLinkId()); // ID link thanh toán để cập nhật status sau
        // paymentResponse.put("checkoutUrl", response.getCheckoutUrl()); // Có thể trả thêm checkoutUrl nếu cần dự phòng

        return paymentResponse;
        // --- Kết thúc thay đổi ---
    }

    @Transactional
    public String updatePaymentStatusAfterSuccess(String paymentLinkId) {
        Payment payment = paymentRepository.findByPaymentLinkId(paymentLinkId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thanh toán với Link ID: " + paymentLinkId));

        if ("PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
            payment.setPaymentStatus("PAID");
            paymentRepository.save(payment);

            Invoice invoice = payment.getInvoice();
            if (invoice != null) {
                invoice.setStatus("PAID");
                invoiceRepository.save(invoice);

                Maintenance maintenance = invoice.getMaintenance();
                if (maintenance != null) {
                    // --- Thêm cập nhật trạng thái Maintenance ---
                    // Chỉ cập nhật Maintenance thành Completed nếu nó chưa Completed hoặc Cancelled
                    if (!"Completed".equalsIgnoreCase(maintenance.getStatus()) && !"Cancelled".equalsIgnoreCase(maintenance.getStatus())) {
                        maintenance.setStatus("Completed");
                        maintenanceRepository.save(maintenance); // Lưu lại Maintenance
                    }
                    // --- Kết thúc cập nhật Maintenance ---

                    // Cập nhật Order (nếu có)
                    if (maintenance.getOrders() != null) {
                        Orders order = maintenance.getOrders();
                        // Đồng bộ trạng thái Order với Maintenance (hoặc logic riêng nếu cần)
                        if (!"Completed".equalsIgnoreCase(order.getStatus()) && !"Cancelled".equalsIgnoreCase(order.getStatus())) {
                            order.setStatus("Completed"); // Cập nhật trạng thái Order
                            order.setPaymentStatus(true); // Cập nhật trạng thái thanh toán Order
                            ordersRepository.save(order);
                        }
                        return "Cập nhật trạng thái thanh toán thành công cho Payment ID: " + payment.getPaymentID() + " (Link ID: " + paymentLinkId + ")";
                    } else {
                        // Vẫn trả về thành công nếu không có Order liên quan
                        return "Cập nhật trạng thái Payment và Maintenance thành công, nhưng không tìm thấy Order liên quan.";
                    }
                } else {
                    return "Cập nhật trạng thái Payment và Invoice thành công, nhưng không tìm thấy Maintenance liên quan.";
                }
            } else {
                return "Cập nhật trạng thái Payment thành công, nhưng không tìm thấy Invoice liên quan.";
            }
        } else {
            return "Trạng thái thanh toán đã được xử lý trước đó: " + payment.getPaymentStatus();
        }
    }
}