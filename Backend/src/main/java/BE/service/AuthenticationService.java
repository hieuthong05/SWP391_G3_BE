package BE.service;


import BE.entity.*;
import BE.model.AdminDTO;
import BE.model.CustomerDTO;
import BE.model.EmployeeDTO;
import BE.model.UserDTO;
import BE.model.request.LoginRequest;
import BE.model.response.UserResponse;
import BE.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.proxy.HibernateProxy;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuthenticationService implements UserDetailsService {
    //xử lý logic của controller đưa qua
    @Autowired
    AuthenticationRepository authenticationRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ShiftRepository shiftRepository;

    @Autowired
    ServiceCenterRepository serviceCenterRepository;

    @Transactional
    public CustomerDTO registerCus(CustomerDTO customerDTO){
        //Xử lý logic của register
        Customer customer = convertCustomer(customerDTO);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        Customer savedCustomer = customerRepository.save(customer);

        User user = modelMapper.map(customerDTO,User.class);
        user.setPassword(savedCustomer.getPassword());
        user.setRole("customer");
        user.setRefId(savedCustomer.getCustomerID());
        user.setRefType("customer");
        user.setStatus(true);
        authenticationRepository.save(user);

        return convertToDTO(savedCustomer);
    }

    @Transactional
    public AdminDTO registerAdmin(AdminDTO adminDTO){
        //Xử lý logic của register
        Admin admin = convertAdmin(adminDTO);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        Admin savedAdmin = adminRepository.save(admin);

        User user = modelMapper.map(adminDTO,User.class);
        user.setPassword(savedAdmin.getPassword());
        user.setRole("admin");
        user.setRefId(savedAdmin.getAdminID());
        user.setRefType("admin");
        user.setStatus(true);
        authenticationRepository.save(user);

        return convertToDTO(savedAdmin);
    }

    public EmployeeDTO registerEmp(EmployeeDTO employeeDTO)
    {
        employeeDTO.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));

        Employee convertEmployee = convertEmployee(employeeDTO);
        Shift shift = shiftRepository.findById(employeeDTO.getShift()).orElseThrow(() -> new RuntimeException("Shift not found"));
        ServiceCenter serviceCenter = serviceCenterRepository.findById(employeeDTO.getServiceCenter()).orElseThrow(() -> new RuntimeException("Service Center not found"));
        convertEmployee.setShift(shift);
        convertEmployee.setServiceCenter(serviceCenter);
        employeeRepository.save(convertEmployee);
        return employeeDTO;
    }

    public UserResponse login(LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getPhone(),
                        loginRequest.getPassword()
                )
        );
        User user = authenticationRepository.findUserByPhone(loginRequest.getPhone());
        Object userDetail = getEntityInfo(user);

        if(userDetail==null){
            throw new RuntimeException("Not matching");
        }

        UserResponse userResponse = switch (user.getRole().toLowerCase()) {
            case "customer" -> modelMapper.map((Customer) userDetail, UserResponse.class);
            case "admin", "center_admin" -> modelMapper.map((Admin) userDetail, UserResponse.class);
            case "staff", "technician" -> modelMapper.map((Employee) userDetail, UserResponse.class);
            default -> throw new RuntimeException("Role không hợp lệ");
        };
        userResponse.setUserID(user.getUserID());
        userResponse.setRole(user.getRole());
        userResponse.setPhone(user.getUsername());

        String token = tokenService.generateToken(user);
        userResponse.setToken(token);

        return userResponse;
    }

    public List<User> getAllUser(){
        List<User> users = authenticationRepository.findAll();
        return users;
    }

    public Object getEntityInfo(User request){
        String role = request.getRole();
        Long refId = request.getRefId();

        if(role==null||role.isEmpty()){
            role="customer";
        }
        Object entity;
        switch (role.toLowerCase()) {
            case "customer":
                entity = customerRepository.findById(refId).orElse(null);
                break;
            case "staff":
            case "technician":
                entity = employeeRepository.findById(refId).orElse(null);
                break;
            case "center_admin":
            case "admin":
                entity = adminRepository.findById(refId).orElse(null);
                break;
            default:
                throw new RuntimeException("Role không hợp lệ");
        }
        return entity;

    }

    public UserResponse getUserInfo(User user) {
        Object userDetail = getEntityInfo(user);

        if(userDetail == null) {
            throw new RuntimeException("User info not found !");
        }

        UserResponse userResponse = switch (user.getRole().toLowerCase()) {
            case "customer" -> modelMapper.map((Customer) userDetail, UserResponse.class);
            case "admin" -> modelMapper.map((Admin) userDetail, UserResponse.class);
            case "staff", "technician" -> modelMapper.map((Employee) userDetail, UserResponse.class);
            default -> throw new RuntimeException("Role không hợp lệ");
        };

        userResponse.setUserID(user.getUserID());
        userResponse.setRole(user.getRole());
        userResponse.setPhone(user.getUsername());

        return userResponse;
    }


    public Customer convertCustomer(CustomerDTO request){
        return modelMapper.map(request,Customer.class);
    }

    public Admin convertAdmin(AdminDTO request){
        return modelMapper.map(request,Admin.class);
    }

    public Employee convertEmployee(EmployeeDTO employeeDTO)
    {
        return modelMapper.map(employeeDTO, Employee.class);
    }

    public CustomerDTO convertToDTO(Customer customer){
        return modelMapper.map(customer,CustomerDTO.class);
    }

    public AdminDTO convertToDTO(Admin admin){
        return modelMapper.map(admin,AdminDTO.class);
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return (UserDetails) authenticationRepository.findUserByPhone(phone);
    }
}
