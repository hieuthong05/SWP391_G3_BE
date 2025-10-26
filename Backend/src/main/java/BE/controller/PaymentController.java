package BE.controller;

import BE.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@SecurityRequirement(name = "api")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //định dạng dữ liệu gửi từ frontend
    public record CreatePaymentLinkRequest(Long invoiceId) {}

    @PostMapping("/create")
    public ResponseEntity<String> createPaymentLink(@RequestBody CreatePaymentLinkRequest request) {
        try {
            String checkoutUrl = paymentService.createPaymentLink(request.invoiceId());
            return ResponseEntity.ok(checkoutUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi tạo link thanh toán: " + e.getMessage());
        }
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<Map<String, String>> handlePayOSWebhook(@RequestBody JsonNode webhookData, @RequestHeader("x-payos-signature") String signature) {
        try {
            paymentService.handleWebhook(webhookData, signature);
            return ResponseEntity.ok(Map.of("message", "Webhook processed successfully"));
        } catch (Exception e) {
            System.err.println("Error processing PayOS webhook: " + e.getMessage());
            // Trả về lỗi 500 hoặc 400 tùy thuộc vào loại lỗi
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}