package BE.controller;

import BE.model.DTO.InvoiceDTO;
import BE.model.response.InvoiceResponse;
import BE.service.InvoiceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class InvoiceController {

    @Autowired
    private final InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceDTO requestDTO) {
        try {
            InvoiceResponse newInvoice = invoiceService.createInvoice(requestDTO.getMaintenanceId());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invoice created successfully!");
            response.put("data", newInvoice);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/getby/{id}")
    public ResponseEntity<?> getInvoiceById(@PathVariable Long id) {
        try {
            InvoiceResponse invoice = invoiceService.getInvoiceById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get invoice successfully!");
            response.put("data", invoice);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/getby/maintenance/{maintenanceId}")
    public ResponseEntity<?> getInvoiceByMaintenanceId(@PathVariable Long maintenanceId) {
        try {
            InvoiceResponse invoice = invoiceService.getInvoiceByMaintenanceId(maintenanceId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get invoice by maintenance ID successfully!");
            response.put("data", invoice);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllInvoices() {
        try {
            List<InvoiceResponse> invoices = invoiceService.getAllInvoices();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get all invoices successfully!");
            response.put("data", invoices);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}