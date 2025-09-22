package com.example.Login.Service;


import com.example.Login.DTO.LoginRequest;
import com.example.Login.Entity.Customer;
import com.example.Login.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer login(LoginRequest loginRequest) throws Exception {
        String input = loginRequest.getEmailOrPhone();
        String password = loginRequest.getPassword();

        return customerRepository.findByEmailAndPassword(input, password)
                .or(() -> customerRepository.findByPhoneAndPassword(input, password))
                .orElseThrow(() -> new Exception("Sai email/phone hoặc password"));
    }
}
