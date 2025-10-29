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
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

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
    public String createPaymentLink(Long invoiceId) throws Exception {
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
        String returnUrl = "http://localhost:5173/payment-success";
        String cancelUrl = "http://localhost:5173/payment-cancel";

        List<ItemData> items = new ArrayList<>();
        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
            String itemName = "Hang muc bao duong/sua chua";

            //tạo ItemData
            ItemData item = ItemData.builder()
                    .name(itemName)
                    .quantity(detail.getQuantity())
                    .price((int) detail.getUnitPrice())
                    .build();
            items.add(item);
        }

        //tạo PaymentData
        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount((int) savedPayment.getAmount())
                .description(description)
                .items(items)
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .build();

        // Gọi PayOS và nhận về CheckoutResponseData
        CheckoutResponseData response = this.payOS.createPaymentLink(paymentData);

        if (response == null) {
            throw new Exception("Lỗi khi tạo link thanh toán từ PayOS, response trả về null.");
        }

        savedPayment.setPaymentLinkId(response.getPaymentLinkId());
        paymentRepository.save(savedPayment);

        return response.getCheckoutUrl();
    }

    @Transactional
    public String updatePaymentStatusAfterSuccess(String paymentLinkId) { // Thay Long paymentId bằng String paymentLinkId
        // Tìm Payment bằng paymentLinkId thay vì paymentId
        Payment payment = paymentRepository.findByPaymentLinkId(paymentLinkId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thanh toán với Link ID: " + paymentLinkId));

        // Phần còn lại giữ nguyên logic cập nhật
        if ("PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
            payment.setPaymentStatus("PAID");
            paymentRepository.save(payment);

            Invoice invoice = payment.getInvoice();
            if (invoice != null) {
                invoice.setStatus("PAID");
                invoiceRepository.save(invoice);

                Maintenance maintenance = invoice.getMaintenance();
                if (maintenance != null && maintenance.getOrders() != null) {
                    Orders order = maintenance.getOrders();

                    if (!"Completed".equalsIgnoreCase(order.getStatus()) && !"Cancelled".equalsIgnoreCase(order.getStatus())) {
                        order.setStatus("Completed");
                        order.setPaymentStatus(true);
                        ordersRepository.save(order);
                    }
                    // Trả về thông báo với Payment ID (Long) để dễ theo dõi
                    return "Cập nhật trạng thái thanh toán thành công cho Payment ID: " + payment.getPaymentID() + " (Link ID: " + paymentLinkId + ")";
                } else {
                    return "Cập nhật trạng thái Payment thành công, nhưng không tìm thấy Order liên quan.";
                }
            } else {
                return "Cập nhật trạng thái Payment thành công, nhưng không tìm thấy Invoice liên quan.";
            }
        } else {
            return "Trạng thái thanh toán đã được xử lý trước đó: " + payment.getPaymentStatus();
        }
    }
}