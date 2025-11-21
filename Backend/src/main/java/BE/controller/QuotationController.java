package BE.controller;

import BE.model.DTO.QuotationDTO;
import BE.model.response.QuotationResponse;
import BE.service.QuotationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class QuotationController {

    @Autowired
    private final QuotationService quotationService;

    @PreAuthorize("hasAnyAuthority('staff', 'admin', 'technician')")
    @PostMapping("/create")
    public ResponseEntity<?> createQuotation(@Valid @RequestBody QuotationDTO requestDTO) {
        try {
            QuotationResponse newQuotation = quotationService.createQuotation(requestDTO.getMaintenanceId());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quotation created successfully!");
            response.put("data", newQuotation);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuotationById(@PathVariable Long id) {
        try {
            QuotationResponse quotation = quotationService.getQuotationById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get quotation successfully!");
            response.put("data", quotation);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, String>> confirmQuotation(
            @PathVariable Long id,
            @RequestParam boolean approved) {
        try {
            quotationService.confirmQuotation(id, approved);
            String message = approved ? "Quotation approved successfully." : "Quotation rejected.";
            return ResponseEntity.ok(Map.of("message", message));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @GetMapping
    public ResponseEntity<List<QuotationResponse>> getAllQuotations() {
        List<QuotationResponse> quotations = quotationService.getAllQuotation();
        return ResponseEntity.ok(quotations);
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @GetMapping("/maintenance/{maintenanceId}")
    public ResponseEntity<?> getQuotationByMaintenanceId(@PathVariable Long maintenanceId) {
        try {
            QuotationResponse quotation = quotationService.getQuotationByMaintenanceId(maintenanceId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get quotation by maintenance ID successfully!");
            response.put("data", quotation);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getQuotationByOrderId(@PathVariable Long orderId) {
        try {
            QuotationResponse quotation = quotationService.getQuotationByOrderId(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get quotation by order ID successfully!");
            response.put("data", quotation);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

//    @PutMapping("/maintenance/{maintenanceId}")
//    public ResponseEntity<?> updateQuotationByMaintenanceId(@PathVariable Long maintenanceId) {
//        try {
//            QuotationResponse updatedQuotation = quotationService.updateQuotationByMaintenanceId(maintenanceId);
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Quotation updated and recalculated successfully!");
//            response.put("data", updatedQuotation);
//            return ResponseEntity.ok(response);
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
//        }
//    }

}