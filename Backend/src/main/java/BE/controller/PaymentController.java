package BE.controller;

import BE.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@SecurityRequirement(name = "api")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // DTO này dùng để định dạng dữ liệu gửi từ frontend lên
    // Frontend sẽ gửi một JSON có dạng: { "invoiceId": 123 }
    public record CreatePaymentLinkRequest(Long invoiceId) {}

    @PostMapping("/create")
    public ResponseEntity<String> createPaymentLink(@RequestBody CreatePaymentLinkRequest request) {
        try {
            // gọi service để thực hiện toàn bộ logic
            String checkoutUrl = paymentService.createPaymentLink(request.invoiceId());
            // Trả về checkoutUrl cho frontend
            return ResponseEntity.ok(checkoutUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi tạo link thanh toán: " + e.getMessage());
        }
    }
}