package BE.controller;

import BE.model.request.ConfirmBookingRequest;
import BE.model.response.BookingResponse;
import BE.model.response.ConfirmBookingResponse;
import BE.model.response.MaintenanceResponse;
import BE.service.MaintenanceService;
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
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @PreAuthorize("hasAnyAuthority('staff', 'admin', 'technician')")
    @SecurityRequirement(name = "api")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMaintenances()
    {

        List<MaintenanceResponse> maintenances = maintenanceService.getAllMaintenances();

        Map<String, Object> response = new HashMap<>();
        response.put("Total Maintenances", maintenances.size());
        response.put("Maintenances", maintenances);

        return ResponseEntity.ok(response);
    }


//     * Lấy maintenance theo ID
//     * GET /api/maintenances/{maintenanceId}

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @SecurityRequirement(name = "api")
    @GetMapping("/{maintenanceId}")
    public ResponseEntity<Map<String, Object>> getMaintenanceById(@PathVariable Long maintenanceId)
    {
        try
        {
            MaintenanceResponse maintenance = maintenanceService.getMaintenanceById(maintenanceId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get maintenance successfully");
            response.put("data", maintenance);

            return ResponseEntity.ok(response);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('staff', 'admin', 'technician')")
    @SecurityRequirement(name = "api")
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<Map<String, Object>> getMaintenancesByTechnicianId(@PathVariable Long technicianId)
    {

        List<MaintenanceResponse> maintenances = maintenanceService.getMaintenancesByTechnicianId(technicianId);

        Map<String, Object> response = new HashMap<>();
        response.put("Technician Id", technicianId);
        response.put("Total Maintenances", maintenances.size());
        response.put("Maintenances", maintenances);

        return ResponseEntity.ok(response);
    }

     //* Confirm booking và tạo maintenance
     //* POST /api/bookings/confirm

    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @PostMapping("/confirm")
    public ResponseEntity<ConfirmBookingResponse> confirmBooking(
            @Valid @RequestBody ConfirmBookingRequest request)
    {
        ConfirmBookingResponse response = maintenanceService
                .confirmBookingAndCreateMaintenance(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


     //* Lấy maintenance theo order ID
     //* GET /api/bookings/{orderId}/maintenance
     @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @SecurityRequirement(name = "api")
    @GetMapping("/{orderId}/maintenance")
    public ResponseEntity<Map<String, Object>> getMaintenanceByOrderId(
            @PathVariable Long orderId)
    {
        var maintenance = maintenanceService.getMaintenanceByOrderId(orderId);

        Map<String, Object> response = Map.of(
                "maintenanceId", maintenance.getMaintenanceID(),
                "orderId", maintenance.getOrders().getOrderID(),
                "employeeId", maintenance.getEmployee().getEmployeeID(),
                "employeeName", maintenance.getEmployee().getName(),
                "vehicleId", maintenance.getVehicle().getVehicleID(),
                "status", maintenance.getStatus(),
                "description", maintenance.getDescription() != null ? maintenance.getDescription() : "",
                "cost", maintenance.getCost(),
                "startTime", maintenance.getStartTime(),
                "notes", maintenance.getNotes() != null ? maintenance.getNotes() : ""
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @SecurityRequirement(name = "api")
    @PutMapping("/{maintenanceID}/set-status/in-progress")
    public ResponseEntity<Map<String, String>> setInProgress(@PathVariable Long maintenanceID)
    {
        maintenanceService.setInProgress(maintenanceID);
        return ResponseEntity.ok(Map.of("message", "Set Status 'In Progress' successfully!"));
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @SecurityRequirement(name = "api")
    @PutMapping("/{maintenanceID}/set-status/waiting-for-payment")
    public ResponseEntity<Map<String, String>> setWaitingForPayment(@PathVariable Long maintenanceID)
    {
        maintenanceService.setWaitingForPayment(maintenanceID);
        return ResponseEntity.ok(Map.of("message", "Set Status 'Waiting For Payment' successfully!"));
    }

    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @PutMapping("/{maintenanceID}/set-status/completed")
    public ResponseEntity<Map<String, String>> setCompleted(@PathVariable Long maintenanceID)
    {
        maintenanceService.setCompleted(maintenanceID);
        return ResponseEntity.ok(Map.of("message", "Set Status 'Completed' successfully!"));
    }
}
