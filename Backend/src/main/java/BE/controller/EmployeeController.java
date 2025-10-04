package BE.controller;

import BE.model.EmployeeDTO;
import BE.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/api/employee/register")
    public ResponseEntity registerEmp(@Valid @RequestBody EmployeeDTO employeeDTO)
    {
        EmployeeDTO newEmp = authenticationService.registerEmp(employeeDTO);
        return ResponseEntity.ok(newEmp);
    }

}
