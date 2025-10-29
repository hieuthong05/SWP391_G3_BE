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
    public ResponseEntity<Map<String, Object>> createPaymentLink(@RequestBody CreatePaymentLinkRequest request) {
        try {
            // Nhận Map từ service
            Map<String, Object> paymentResponse = paymentService.createPaymentLink(request.invoiceId());
            // Trả về Map đó dưới dạng JSON
            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về lỗi dưới dạng Map cho thống nhất
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tạo link thanh toán: " + e.getMessage()));
        }
    }
    @PutMapping("/update-status/success/{paymentLinkId}")
    public ResponseEntity<?> updateStatusAfterSuccess(@PathVariable String paymentLinkId) { // Thay Long paymentId bằng String paymentLinkId
        try {
            // Gọi hàm service đã sửa đổi
            String message = paymentService.updatePaymentStatusAfterSuccess(paymentLinkId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi không xác định khi cập nhật trạng thái: " + e.getMessage()));
        }
    }

}