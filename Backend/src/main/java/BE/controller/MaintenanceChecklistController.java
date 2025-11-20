package BE.controller;

import BE.model.DTO.MaintenanceChecklistDTO;
import BE.model.response.MaintenanceChecklistResponse;
import BE.service.MaintenanceChecklistService;
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
@RequestMapping("/api/maintenances/{maintenanceId}/checklist-items") // Endpoint lồng nhau
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class MaintenanceChecklistController {

    @Autowired
    private final MaintenanceChecklistService maintenanceChecklistService;

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @PostMapping(name="/createAndUpdateItem")
    public ResponseEntity<?> addOrUpdateChecklistItem(
            @PathVariable Long maintenanceId,
            @Valid @RequestBody MaintenanceChecklistDTO checklistDTO) {
        try {
            MaintenanceChecklistResponse response = maintenanceChecklistService.addOrUpdateChecklistItem(maintenanceId, checklistDTO);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Checklist item added/updated successfully!");
            result.put("data", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Log lỗi ra để debug
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to process checklist item: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('customer', 'technician', 'staff', 'admin')")
    @GetMapping(name="/getChecklistItemById")
    public ResponseEntity<?> getChecklistItemsByMaintenanceId(@PathVariable Long maintenanceId) {
        try {
            List<MaintenanceChecklistResponse> items = maintenanceChecklistService.getChecklistItemsByMaintenanceId(maintenanceId);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Checklist items fetched successfully!");
            result.put("maintenanceId", maintenanceId);
            result.put("totalItems", items.size());
            result.put("data", items);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch checklist items: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @DeleteMapping("/{maintenanceChecklistId}")
    public ResponseEntity<?> deleteChecklistItem(
            @PathVariable Long maintenanceId, // Vẫn cần maintenanceId để xác định context, dù không dùng trực tiếp trong service
            @PathVariable Long maintenanceChecklistId) {
        try {
            maintenanceChecklistService.deleteChecklistItem(maintenanceChecklistId);
            return ResponseEntity.ok(Map.of("message", "Checklist item removed successfully!"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) { // Bắt lỗi nếu không được phép xóa
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to remove checklist item: " + e.getMessage()));
        }
    }
}