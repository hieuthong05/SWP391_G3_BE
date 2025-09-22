package com.example.Login.Controller;


import com.example.Login.DTO.LoginRequest;
import com.example.Login.Entity.Customer;
import com.example.Login.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Customer customer = authService.login(loginRequest);
            return ResponseEntity.ok(customer); // có thể trả JWT token thay vì cả object
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

