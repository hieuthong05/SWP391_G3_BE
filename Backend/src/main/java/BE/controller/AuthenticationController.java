package BE.controller;

import BE.entity.User;
import BE.model.request.LoginRequest;
import BE.model.response.UserResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import BE.service.AuthenticationService;

import java.util.List;


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
}
