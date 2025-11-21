package BE.controller;


import BE.entity.Customer;
import BE.model.response.CustomerResponse;
import BE.model.response.VehicleResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @GetMapping("/getAll")
    public ResponseEntity<List<CustomerResponse>> getAll(){
        List<CustomerResponse> responses = customerService.getAllCustomer();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
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

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin')")
    @SecurityRequirement(name = "api")
    @PatchMapping("/update/{id}") // Đổi từ PutMapping -> PatchMapping
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO dto){
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
        catch (Exception e) {
            // Bắt thêm lỗi 500 để debug xem nó là lỗi gì
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('admin')")
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
