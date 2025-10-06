package BE.controller;

import BE.entity.Admin;
import BE.entity.Employee;
import BE.model.AdminDTO;
import BE.model.EmployeeDTO;
import BE.model.response.AdminResponse;
import BE.model.response.EmployeeResponse;
import BE.service.AdminService;
import BE.service.AuthenticationService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authenticationService;

    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<AdminDTO> registerAd(@Valid @RequestBody AdminDTO adminDTO){
        AdminDTO newAd = authenticationService.registerAdmin(adminDTO);
        return ResponseEntity.ok(newAd);
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/getby/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        AdminResponse adminResponse = adminService.getAdminById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get admin successfully");
        response.put("data", adminResponse);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/getAll")
    public ResponseEntity<List<AdminResponse>> getAllAdmin(){
        List<AdminResponse> responses = adminService.getAllAdmin();
        return ResponseEntity.ok(responses);
    }

    @SecurityRequirement(name = "api")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id,@Valid @RequestBody AdminDTO dto){
        try {
            AdminResponse updatedAdmin = adminService.updateAdmin(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin updated successfully");

            return ResponseEntity.ok(Map.of("message",
                                            "Admin account updated successfully",
                                            "admin",updatedAdmin
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
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        try {
            adminService.deleteAdmin(id);
            return ResponseEntity.ok("Admin with ID " + id + " has been deactivated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }


}
