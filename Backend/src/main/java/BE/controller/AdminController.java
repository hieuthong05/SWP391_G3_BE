package BE.controller;

import BE.model.DTO.AdminDTO;
import BE.model.response.AdminResponse;
import BE.service.AdminService;
import BE.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('admin')")
public class AdminController {

    private final AuthenticationService authenticationService;

    private final AdminService adminService;

    @SecurityRequirement(name = "api")
    @GetMapping("/getby/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id){
        AdminResponse admin = adminService.getAdminById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get admin successfully");
        response.put("data", admin);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "api")
    @PostMapping("/register")
    public ResponseEntity registerAdmin(@Valid @RequestBody AdminDTO adminDTO){
        AdminDTO newAdmin = authenticationService.registerAdmin(adminDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAdmin);
    }

    @SecurityRequirement(name = "api")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id,@Valid @RequestBody AdminDTO dto){
        try{
            AdminResponse updatedAdmin = adminService.updateAdmin(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin updated successfully");
            response.put("data", updatedAdmin);
            return ResponseEntity.ok(response);
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
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
