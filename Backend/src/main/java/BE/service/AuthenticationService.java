package BE.service;


import BE.entity.*;
import BE.exception.AuthenticationException;
import BE.model.DTO.AdminDTO;
import BE.model.DTO.CustomerDTO;
import BE.model.DTO.EmployeeDTO;
import BE.model.EmailDetail;
import BE.model.request.LoginRequest;
import BE.model.response.UserResponse;
import BE.repository.*;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

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
        convertEmployee.setStatus(true);
        Employee savedEmployee = employeeRepository.save(convertEmployee);

        User user = modelMapper.map(employeeDTO,User.class);
        user.setPassword(savedEmployee.getPassword());
        user.setRole(savedEmployee.getRole());
        user.setRefId(savedEmployee.getEmployeeID());
        user.setRefType(savedEmployee.getRole());
        user.setStatus(true);
        authenticationRepository.save(user);

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
            case "customer" -> {
                Customer cus = (Customer) userDetail;
                UserResponse res = modelMapper.map(cus, UserResponse.class);
                res.setName(cus.getName());
                res.setEmail(cus.getEmail());
                res.setPhone(cus.getPhone());
                res.setGender(cus.getGender());
                res.setAddress(cus.getAddress());
                res.setBirth(cus.getBirth());
                yield res;
            }
            case "admin", "center_admin" -> modelMapper.map((Admin) userDetail, UserResponse.class);
            case "staff", "technician" -> {
                UserResponse res = modelMapper.map((Employee) userDetail, UserResponse.class);
                Employee emp = (Employee) userDetail;
                res.setName(emp.getName());
                res.setEmail(emp.getEmail());
                res.setPhone(emp.getPhone());
                res.setGender(emp.getGender());
                res.setPhone(emp.getPhone());
                if (emp.getServiceCenter() != null) {
                    res.setServiceCenter(emp.getServiceCenter().getServiceCenterID());
                }
                if (emp.getShift() != null) {
                    res.setShift(emp.getShift().getShiftID());
                }
                yield res;
            }
            default -> throw new RuntimeException("Role không hợp lệ");
        };

        userResponse.setRole(user.getRole());
        if (userResponse.getPhone() == null || userResponse.getPhone().isEmpty()) {
            userResponse.setPhone(user.getUsername());
        }

        String token = tokenService.generateToken(user);
        userResponse.setToken(token);

        return userResponse;
    }

    @Transactional
    public String forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return "Nếu tài khoản tồn tại, email đặt lại mật khẩu đã được gửi.";
        }

        // Tạo token reset mật khẩu (15 phút)
        String resetToken = Jwts.builder()
                .subject(user.getPhone())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 phút
                .signWith(tokenService.getSignInKey())
                .compact();

        // Lấy thông tin chi tiết (tên, email)
        Object userDetail = getEntityInfo(user);
        String userEmail = "";
        String userName = "";

        if (userDetail instanceof Customer customer) {
            userEmail = customer.getEmail();
            userName = customer.getName();
        } else if (userDetail instanceof Employee employee) {
            userEmail = employee.getEmail();
            userName = employee.getName();
        } else if (userDetail instanceof Admin admin) {
            userEmail = admin.getEmail();
            userName = admin.getName();
        } else {
            throw new RuntimeException("Không tìm thấy thông tin người dùng để gửi email.");
        }

        if (userEmail == null || userEmail.isEmpty()) {
            throw new RuntimeException("Người dùng không có email để đặt lại mật khẩu.");
        }

        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setRecipient(userEmail);
        emailDetail.setSubject("Yêu cầu đặt lại mật khẩu (EV Care)");
        emailDetail.setFullName(userName);
        emailDetail.setLink(resetLink);

        System.out.println("Gửi email reset tới: " + userEmail);
        System.out.println("Reset Token (gửi qua link): " + resetToken);
        emailService.sendPasswordResetEmail(emailDetail);

        return "Nếu tài khoản tồn tại, email đặt lại mật khẩu đã được gửi.";
    }
    @Transactional
    public String resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.isEmpty() || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        String phone;
        try {
            // 1. Xác thực token
            phone = tokenService.extractPhone(token);
            if (tokenService.isTokenExpired(token)) {
                throw new RuntimeException("Token đã hết hạn.");
            }
        } catch (Exception e) {
            throw new AuthenticationException("Token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
        }

        // 2. Tìm User chung
        User user = authenticationRepository.findUserByPhone(phone);
        if (user == null) {
            throw new EntityNotFoundException("Không tìm thấy người dùng cho token này.");
        }

        // 3. Mã hóa và cập nhật mật khẩu
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);
        authenticationRepository.save(user);

        // 4. Cập nhật mật khẩu ở bảng cụ thể (Customer, Admin, Employee)
        Object userDetail = getEntityInfo(user);
        if (userDetail instanceof Customer customer) {
            customer.setPassword(hashedNewPassword);
            customerRepository.save(customer);
        } else if (userDetail instanceof Employee employee) {
            employee.setPassword(hashedNewPassword);
            employeeRepository.save(employee);
        } else if (userDetail instanceof Admin admin) {
            admin.setPassword(hashedNewPassword);
            adminRepository.save(admin);
        }

        return "Cập nhật mật khẩu thành công.";
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

    public User getCurrentUser(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return null;
        }
        return (User) authentication.getPrincipal();
    }

    public UserResponse getUserInfo(User user) {

        if (user.getRefId() == null)
        {
            System.out.println("⚠️ OAuth2 user detected (no refId): " + user.getEmail());

            UserResponse userResponse = new UserResponse();
            userResponse.setUserID(user.getUserID());
            userResponse.setName(user.getFullName());
            userResponse.setEmail(user.getEmail());
            userResponse.setRole(user.getRole());
            userResponse.setPhone(user.getPhone());

            return userResponse;
        }

        Object userDetail = getEntityInfo(user);

        if(userDetail == null) {
            throw new RuntimeException("User info not found !");
        }

        UserResponse userResponse = switch (user.getRole().toLowerCase()) {
            case "customer" -> {
                Customer cus = (Customer) userDetail;
                UserResponse res = modelMapper.map(cus, UserResponse.class);
                res.setName(cus.getName());
                res.setEmail(cus.getEmail());
                res.setPhone(cus.getPhone());
                res.setGender(cus.getGender());
                res.setAddress(cus.getAddress());
                res.setBirth(cus.getBirth());
                yield res;
            }
            case "admin" -> modelMapper.map((Admin) userDetail, UserResponse.class);
            case "staff", "technician" -> {
                UserResponse res = modelMapper.map((Employee) userDetail, UserResponse.class);
                Employee emp = (Employee) userDetail;
                res.setName(emp.getName());
                res.setEmail(emp.getEmail());
                res.setPhone(emp.getPhone());
                res.setGender(emp.getGender());
                res.setPhone(emp.getPhone());
                if (emp.getServiceCenter() != null) {
                    res.setServiceCenter(emp.getServiceCenter().getServiceCenterID());
                }
                if (emp.getShift() != null) {
                    res.setShift(emp.getShift().getShiftID());
                }
                yield res;
            }
            default -> throw new RuntimeException("Role không hợp lệ");
        };

        userResponse.setUserID(user.getUserID());
        userResponse.setRole(user.getRole());
        if (userResponse.getPhone() == null || userResponse.getPhone().isEmpty()) {
            userResponse.setPhone(user.getUsername());
        }

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
