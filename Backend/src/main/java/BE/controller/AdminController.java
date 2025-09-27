package BE.controller;

import BE.model.AdminDTO;
import BE.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/api/admin/register")
    public ResponseEntity<AdminDTO> registerCus(@Valid @RequestBody AdminDTO adminDTO){
        AdminDTO newAd = authenticationService.registerAdmin(adminDTO);
        return ResponseEntity.ok(newAd);
    }

}
