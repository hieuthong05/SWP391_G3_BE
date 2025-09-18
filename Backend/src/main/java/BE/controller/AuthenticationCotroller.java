package BE.controller;

import BE.entity.Customer;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import BE.service.AuthenticationService;

import java.util.List;

@RestController
public class AuthenticationCotroller {
    //S.O.L.I.D

    //Controller tương tự ở java dùng để điều hướng request, API => xử lý logic (service) => lưu DB (repository) (JPA)
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/api/auth")
    public ResponseEntity register(@Valid @RequestBody Customer customer){
        //Nhận request từ FE
        //===> đẩy qua AuthenticationService

        Customer newCustomer= authenticationService.register(customer);
        return ResponseEntity.ok(newCustomer);
    }

    @GetMapping("/api/auth")
    public ResponseEntity getALlAccount(){
        List<Customer> accountList = authenticationService.getAllAccount();
        return ResponseEntity.ok(accountList);
    }
}
