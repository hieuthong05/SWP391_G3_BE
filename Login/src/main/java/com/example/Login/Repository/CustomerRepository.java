package com.example.Login.Repository;


import com.example.Login.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailAndPassword(String email, String password);
    Optional<Customer> findByPhoneAndPassword(String phone, String password);
}
