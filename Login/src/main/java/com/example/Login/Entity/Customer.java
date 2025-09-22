package com.example.Login.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Column(name = "customerID")
    private String customerID;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private String password;
    private String address;
    private String gender;

    @Column(name = "birth")
    private String birth;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Boolean status;
}

