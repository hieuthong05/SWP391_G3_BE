package BE.controller;

import BE.model.request.ConfirmBookingRequest;
import BE.model.response.ConfirmBookingResponse;
import BE.service.MaintenanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;


     //* Confirm booking và tạo maintenance
     //* POST /api/bookings/confirm

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
}
