package BE.controller;


import BE.entity.Customer;
import BE.model.response.CustomerResponse;
import BE.service.AuthenticationService;
import BE.service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import BE.model.DTO.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    CustomerService customerService;

    @SecurityRequirement(name = "api")
    @GetMapping("/getby/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id){
        CustomerResponse customer = customerService.getCustomerById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get customer successfully");
        response.put("data", customer);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity registerCustomer(@Valid @RequestBody CustomerDTO customerDTO){
        CustomerDTO newCustomer = authenticationService.registerCus(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
    }

    @SecurityRequirement(name = "api")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id,@Valid @RequestBody CustomerDTO dto){
        try{
            CustomerResponse updatedCustomer = customerService.updateCustomer(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer updated successfully");
            response.put("data", updatedCustomer);
            return ResponseEntity.ok(response);
        }
        catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @SecurityRequirement(name = "api")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
