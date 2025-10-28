package BE.controller;

import BE.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @PutMapping("/update-status/success/{paymentId}")
    public ResponseEntity<?> updateStatusAfterSuccess(@PathVariable Long paymentId) {
        try {
            String message = paymentService.updatePaymentStatusAfterSuccess(paymentId);
            // Trả về JSON object thay vì String đơn thuần
            return ResponseEntity.ok(Map.of("message", message));
        } catch (EntityNotFoundException e) {
            // Trả về 404 nếu không tìm thấy Payment
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Log lỗi và trả về 500 cho các lỗi khác
            e.printStackTrace(); // Nên dùng logger thay vì printStackTrace trong production
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi không xác định khi cập nhật trạng thái: " + e.getMessage()));
        }
    }

}