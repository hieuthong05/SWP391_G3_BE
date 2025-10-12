package BE.controller;


import BE.model.DTO.VehicleDTO;
import BE.model.response.VehicleResponse;
import BE.service.VehicleService;
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
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    @Autowired
    VehicleService vehicleService;

    @SecurityRequirement(name = "api")
    @PostMapping("/create")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleDTO vehicleDTO){
        VehicleResponse newVel = vehicleService.createVehicle(vehicleDTO);
        return ResponseEntity.ok(newVel);
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/getby/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id) {
        VehicleResponse vehicleResponse = vehicleService.getVehicleById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get vehicle successfully !");
        response.put("data", vehicleResponse);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/getAll")
    public ResponseEntity<List<VehicleResponse>> getAllVehicle(){
        List<VehicleResponse> responses = vehicleService.getAllVehicle();
        return ResponseEntity.ok(responses);
    }

    @SecurityRequirement(name = "api")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id,@Valid @RequestBody VehicleDTO dto){
        try {
            VehicleResponse updatedVehicle = vehicleService.updateVehicle(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vehicle updated successfully");

            return ResponseEntity.ok(Map.of("message",
                    "Vehicle account updated successfully",
                    "vehicle",updatedVehicle
            ));
        }
        catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @SecurityRequirement(name = "api")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok("Vehicle with ID " + id + " has been deactivated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/getByCustomerId/{id}")
    public ResponseEntity<?> getVehicleByCustomerId(@PathVariable Long id) {
        List <VehicleResponse> responses = vehicleService.getVehiclesByCustomerId(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get vehicle successfully !");
        response.put("data", responses);
        return ResponseEntity.ok(response);
    }
}
