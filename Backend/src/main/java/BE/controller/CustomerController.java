package BE.controller;


import jakarta.validation.Valid;
import BE.model.CustomerDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController // đánh dấu spring boots hiểu nó là api
public class CustomerController {

    List<CustomerDTO> customers = new ArrayList<>();

    //1. Lấy ds tất cả sv
    // => GET: /api/student
    @GetMapping("/api/customer")
    public ResponseEntity get(){
        return ResponseEntity.ok(customers);
    }
    //1.b. Lấy ra 1 sv bằng id
    // => GET: /api/student/id


    //2. Tạo sv mới
    // => POST: /api/student
    @PostMapping("/api/customer")
    public ResponseEntity create(@Valid@RequestBody CustomerDTO customer){
        customers.add(customer);
        return ResponseEntity.ok(customers);
    }

    //3. Update thông tin 1 sv
    // => PUT: /api/student/id

    //4. Delete thông tin 1 sv
    // => DELETE: /api/student/id
}
