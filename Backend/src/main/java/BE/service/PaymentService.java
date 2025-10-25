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
    public void handleWebhook(JsonNode webhookData, String signature) throws Exception {

        String webhookDataAsString = objectMapper.writeValueAsString(webhookData); // Convert JsonNode sang chuỗi JSON
        String calculatedSignature = calculateHmacSha256(webhookDataAsString, this.payosChecksumKey);

        System.out.println("Received webhook data: " + webhookDataAsString);
        System.out.println("Received signature: " + signature);
        System.out.println("Calculated signature: " + calculatedSignature);

        if (!calculatedSignature.equalsIgnoreCase(signature)) {
            System.err.println("Invalid PayOS webhook signature. Received: " + signature + ", Calculated: " + calculatedSignature);
            // lỗi 400 Bad Request tùy theo PayOS
            throw new SecurityException("Invalid PayOS webhook signature");
        }
        System.out.println("Webhook signature verified successfully.");

        // dữ liệu webhook
        String code = webhookData.path("code").asText(); // "00" là thành công
        JsonNode data = webhookData.path("data");
        if (data.isMissingNode()) { //
            throw new IllegalArgumentException("Webhook data is missing 'data' field.");
        }

        long orderCode = data.path("orderCode").asLong(); //paymentID
        String description = data.path("description").asText();
        String status = data.path("status").asText(); // PAID, CANCELLED

        // Tìm Payment
        Payment payment = paymentRepository.findById(orderCode) //
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thanh toán với ID (orderCode): " + orderCode));

        // Cập nhật Payment
        if ("PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
            boolean paymentSuccess = "PAID".equalsIgnoreCase(status) && "00".equals(code);
            String newPaymentStatus = paymentSuccess ? "PAID" : ("CANCELLED".equalsIgnoreCase(status) ? "CANCELLED" : "FAILED");
            payment.setPaymentStatus(newPaymentStatus);
            paymentRepository.save(payment);

            // Cập nhật Invoice và Orders nếu thành công
            if (paymentSuccess) { //
                Invoice invoice = payment.getInvoice();
                if (invoice != null) {
                    invoice.setStatus("PAID");
                    invoiceRepository.save(invoice);

                    // Cập nhật maintenance
                    Maintenance maintenance = invoice.getMaintenance();
                    if (maintenance.getOrders() != null) {
                        Long orderId = maintenance.getOrders().getOrderID();

                        Orders order = ordersRepository.findById(orderId)
                                .orElse(null);

                        if (order != null) {
                            order.setStatus("Completed");
                            order.setPaymentStatus(true);
                            ordersRepository.save(order);
                        }
                    }
                }
            }
            System.out.println("Payment " + orderCode + " status updated to " + newPaymentStatus);
        } else {
            System.out.println("Payment " + orderCode + " already processed with status: " + payment.getPaymentStatus() + ". Ignoring webhook.");
        }
    }

    private String calculateHmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Chuyển từ byte sang hex
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

}