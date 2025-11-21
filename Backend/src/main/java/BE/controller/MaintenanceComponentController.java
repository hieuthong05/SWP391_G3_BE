package BE.controller;

import BE.model.DTO.MaintenanceComponentDTO;
import BE.model.response.MaintenanceComponentResponse;
import BE.service.MaintenanceComponentService;
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
@RequestMapping("/api/maintenances/{maintenanceId}/components")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class MaintenanceComponentController {

    @Autowired
    private final MaintenanceComponentService maintenanceComponentService;

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @PostMapping
    public ResponseEntity<?> addComponentToMaintenance(
            @PathVariable Long maintenanceId,
            @Valid @RequestBody MaintenanceComponentDTO componentDTO) {
        try {
            MaintenanceComponentResponse response = maintenanceComponentService.addComponentToMaintenance(maintenanceId, componentDTO);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Component added/updated successfully!");
            result.put("data", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to add component: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('customer', 'technician', 'staff', 'admin')")
    @GetMapping
    public ResponseEntity<?> getComponentsByMaintenanceId(@PathVariable Long maintenanceId) {
        try {
            List<MaintenanceComponentResponse> components = maintenanceComponentService.getComponentsByMaintenanceId(maintenanceId);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Components fetched successfully!");
            result.put("maintenanceId", maintenanceId);
            result.put("totalComponents", components.size());
            result.put("data", components);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch components: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @PutMapping("/{maintenanceComponentId}/quantity")
    public ResponseEntity<?> updateComponentQuantity(
            @PathVariable Long maintenanceId,
            @PathVariable Long maintenanceComponentId,
            @RequestBody Map<String, Integer> body) {
        try {
            int quantity = body.get("quantity");
            MaintenanceComponentResponse response = maintenanceComponentService.updateComponentQuantity(maintenanceComponentId, quantity);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Quantity updated successfully!");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update quantity: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @DeleteMapping("/{maintenanceComponentId}")
    public ResponseEntity<?> removeComponentFromMaintenance(
            @PathVariable Long maintenanceId,
            @PathVariable Long maintenanceComponentId) {
        try {
            maintenanceComponentService.removeComponentFromMaintenance(maintenanceComponentId);
            return ResponseEntity.ok(Map.of("message", "Component removed successfully!"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to remove component: " + e.getMessage()));
        }
    }
}