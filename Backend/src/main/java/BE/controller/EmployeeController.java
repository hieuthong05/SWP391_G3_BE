package BE.controller;

import BE.entity.Employee;
import BE.model.DTO.EmployeeDTO;
import BE.model.response.EmployeeResponse;
import BE.service.AuthenticationService;
import BE.service.EmployeeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    EmployeeService employeeService;

    @GetMapping("/getby/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id)
    {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get employee successfully");
        response.put("data", employee);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "api")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEmployees()
    {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();

        Map<String, Object> response = new HashMap<>();
        response.put("Total Employees", employees.size());
        response.put("List Of Employees", employees);

        return ResponseEntity.ok(response);
    }

     //* Lấy active employees theo role (useful cho assign technician)
     //* GET /api/employees/active-by-role/{role}

    @SecurityRequirement(name = "api")
    @GetMapping("/all/technicians")
    public ResponseEntity<Map<String, Object>> getAllActiveTechnicians()
    {
        List<EmployeeResponse> employees =
                employeeService.getAllActiveTechnicians();

        Map<String, Object> response = new HashMap<>();
        response.put("Total Technicians", employees.size());
        response.put("List Of Technicians", employees);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity registerEmp(@Valid @RequestBody EmployeeDTO employeeDTO)
    {
        EmployeeDTO newEmp = authenticationService.registerEmp(employeeDTO);
        return ResponseEntity.ok(newEmp);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO dto)
    {
        try
        {
            Employee updatedEmployee = employeeService.updateEmployee(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Employee updated successfully");
            return ResponseEntity.ok(response);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @SecurityRequirement(name = "api")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable Long Id)
    {
        employeeService.deleteEmployee(Id);
        return ResponseEntity.ok(Map.of("message", "Delete Employee successfully"));
    }

}
