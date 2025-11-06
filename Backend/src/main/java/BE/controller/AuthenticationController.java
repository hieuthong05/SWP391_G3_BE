package BE.controller;

import BE.entity.User;
import BE.exception.AuthenticationException;
import BE.model.request.LoginRequest;
import BE.model.request.ResetPasswordRequest;
import BE.model.response.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import BE.service.AuthenticationService;

import java.util.List;
import java.util.Map;


@RestController
public class AuthenticationController {
    //S.O.L.I.D

    //Controller tương tự ở java dùng để điều hướng request, API => xử lý logic (service) => lưu DB (repository) (JPA)
    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    ModelMapper modelMapper;


    @GetMapping("/api/auth/getAll")
    public ResponseEntity getALlAccount(){
        List<User> accountList = authenticationService.getAllUser();
        return ResponseEntity.ok(accountList);
    }


    @PostMapping("/api/auth/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequest loginRequest){
        UserResponse user = authenticationService.login(loginRequest);
        return ResponseEntity.ok(user);
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/api/auth/getUserInfo")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal User user, HttpServletRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        UserResponse userResponse = authenticationService.getUserInfo(user);

        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userResponse.setToken(token);
        }

        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email là bắt buộc."));
            }

            String message = authenticationService.forgotPassword(email);
            return ResponseEntity.ok(Map.of("message", message));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/api/auth/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        try {
            String message = authenticationService.resetPassword(
                    resetRequest.getToken(),
                    resetRequest.getNewPassword()
            );

            return ResponseEntity.ok(Map.of("message", message));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

        } catch (AuthenticationException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi không xác định: " + e.getMessage()));
        }
    }

}
