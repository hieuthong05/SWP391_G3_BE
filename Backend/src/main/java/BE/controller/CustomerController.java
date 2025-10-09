package BE.controller;


import BE.service.AuthenticationService;
import jakarta.validation.Valid;
import BE.model.DTO.CustomerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    @Autowired
    AuthenticationService authenticationService;


    @PostMapping("/api/customer/register")
    public ResponseEntity<CustomerDTO> registerCus(@Valid@RequestBody CustomerDTO customerDTO){
        CustomerDTO newCus = authenticationService.registerCus(customerDTO);
        return ResponseEntity.ok(newCus);
    }

}
