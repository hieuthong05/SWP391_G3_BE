package com.example.Login.DTO;


import lombok.Data;

@Data
public class LoginRequest {
    private String emailOrPhone;
    private String password;
}

