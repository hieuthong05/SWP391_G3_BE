package BE.service;

import BE.entity.Employee;
import BE.entity.ServiceCenter;
import BE.entity.Shift;
import BE.model.DTO.EmployeeDTO;
import BE.model.response.EmployeeResponse;
import BE.repository.EmployeeRepository;
import BE.repository.ServiceCenterRepository;
import BE.repository.ShiftRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    ServiceCenterRepository serviceCenterRepository;

    @Autowired
    ShiftRepository shiftRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees()
    {
        List<Employee> employees = employeeRepository.findAllWithDetails();

        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllActiveTechnicians()
    {
        List<Employee> employees = employeeRepository.findByRoleAndStatus("technician", true);

        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id)
    {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

        EmployeeResponse employeeResponse = new EmployeeResponse();
        modelMapper.map(employee, employeeResponse);

        if (employee.getServiceCenter() != null)
        {
            employeeResponse.setServiceCenterName(employee.getServiceCenter().getLocation());
        }

        if (employee.getShift() != null)
        {
            employeeResponse.setShiftName(employee.getShift().getName());
        }

        return employeeResponse;
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTO dto)
    {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

        // Check duplicate phone
        if (employeeRepository.findByPhoneAndEmployeeIDNot(dto.getPhone(), id).isPresent())
        {
            throw new IllegalArgumentException("Phone number already exists");
        }

        // Check duplicate email
        if (employeeRepository.findByEmailAndEmployeeIDNot(dto.getEmail(), id).isPresent())
        {
            throw new IllegalArgumentException("Email already exists");
        }

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        modelMapper.map(dto, employee);

        if (dto.getServiceCenter() != null)
        {
            ServiceCenter serviceCenter = serviceCenterRepository.findById(dto.getServiceCenter())
                    .orElseThrow(() -> new EntityNotFoundException("Service Center not found"));
            employee.setServiceCenter(serviceCenter);
        }

        if (dto.getShift() != null)
        {
            Shift shift = shiftRepository.findById(dto.getShift())
                    .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
            employee.setShift(shift);
        }
        return employeeRepository.save(employee);
    }

    //Soft Delete Employee
    @Transactional
    public void deleteEmployee(Long id)
    {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

        if (!employee.isStatus())
        {
            throw new IllegalStateException("Employee is already deactivated");
        }

        employee.setStatus(false);
        employeeRepository.save(employee);
    }

    //Restore Employee
    @Transactional
    public void restoreEmployee(Long id)
    {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));


        if (employee.isStatus())
        {
            throw new IllegalStateException("Employee is already active");
        }

        // Set status = true (restore)
        employee.setStatus(true);
        employeeRepository.save(employee);
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();

        // Basic info
        response.setEmployeeID(employee.getEmployeeID());
        response.setName(employee.getName());
        response.setPhone(employee.getPhone());
        response.setEmail(employee.getEmail());
        response.setGender(employee.getGender());
        response.setRole(employee.getRole());

        response.setSalary(employee.getSalary());
        response.setAddress(employee.getAddress());
        response.setBirth(employee.getBirth());

        // Service Center Name
        if (employee.getServiceCenter() != null)
        {
            response.setServiceCenterName(employee.getServiceCenter().getName());
        }

        // Shift info
        if (employee.getShift() != null)
        {
            response.setShiftName(employee.getShift().getName());
        }

        return response;
    }
}
