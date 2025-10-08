package BE.controller;


import BE.entity.Customer;
import BE.model.AdminDTO;
import BE.model.response.AdminResponse;
import BE.model.response.CustomerResponse;
import BE.repository.CustomerRepository;
import BE.service.AuthenticationService;
import BE.service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import BE.model.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    CustomerService customerService;


    @PostMapping("/api/customer/register")
    public ResponseEntity<CustomerDTO> registerCus(@Valid@RequestBody CustomerDTO customerDTO){
        CustomerDTO newCus = authenticationService.registerCus(customerDTO);
        return ResponseEntity.ok(newCus);
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/getby/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        CustomerResponse customerResponse = customerService.getCustomerById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get customer successfully !");
        response.put("data", customerResponse);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/getAll")
    public ResponseEntity<List<CustomerResponse>> getAllAdmin(){
        List<CustomerResponse> responses = customerService.getAllCustomer();
        return ResponseEntity.ok(responses);
    }

    @SecurityRequirement(name = "api")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id,@Valid @RequestBody CustomerDTO dto){
        try {
            CustomerResponse updatedCustomer = customerService.updateCustomer(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer updated successfully");

            return ResponseEntity.ok(Map.of("message",
                    "Customer account updated successfully",
                    "customer",updatedCustomer
            ));
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
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok("Customer with ID " + id + " has been deactivated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @SecurityRequirement(name="api")
    @GetMapping("/isCustomer")
    public  ResponseEntity<Map<String,Object>>isCustomer(){
        try{
            var currentUser = authenticationService.getCurrentUser();

            boolean isCustomer = currentUser !=null && "customer".equalsIgnoreCase(currentUser.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("isCustomer",isCustomer);
            response.put("message",isCustomer ? "Current is customer." : "User is not customer !");

            return  ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error","Invalid or expired token"));
        }
    }



}
