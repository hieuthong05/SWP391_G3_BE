package BE.service;

import BE.entity.Customer;
import BE.entity.User;
import BE.model.DTO.CustomerDTO;
import BE.model.response.CustomerResponse;
import BE.repository.CustomerRepository;
import BE.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        CustomerResponse customerResponse = new CustomerResponse();
        modelMapper.map(customer, customerResponse);

        return customerResponse;
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerDTO dto) {
        // 1. Lấy Customer từ DB
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        // 🔥 QUAN TRỌNG: Lưu lại Email cũ TRƯỚC KHI MAP dữ liệu mới
        // Để tí nữa lỡ không tìm thấy User theo ID thì dùng Email cũ này tìm
        String oldEmail = customer.getEmail();

        // 2. Check trùng Phone (Chỉ check nếu có thay đổi)
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty() && !dto.getPhone().equals(customer.getPhone())) {
            if (customerRepository.findByPhoneAndCustomerIDNot(dto.getPhone(), id).isPresent()){
                throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
            }
        }

        // 3. Check trùng Email (Chỉ check nếu có thay đổi)
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty() && !dto.getEmail().equals(customer.getEmail())) {
            if (customerRepository.findByEmailAndCustomerIDNot(dto.getEmail(), id).isPresent()){
                throw new IllegalArgumentException("Email đã tồn tại!");
            }
        }

        // 4. Map dữ liệu (Skip Null để không xóa mất dữ liệu cũ)
        ModelMapper partialMapper = new ModelMapper();
        partialMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // Dữ liệu trong biến 'customer' sẽ bị thay đổi tại dòng này
        partialMapper.map(dto, customer);

        // 5. Xử lý Password
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()){
            customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 6. LƯU BẢNG CUSTOMER
        Customer updatedCustomer = customerRepository.save(customer);

        // ==================================================================
        // 7. CHIẾN THUẬT ĐỒNG BỘ USER (Đã gia cố)
        // ==================================================================

        // Bước A: Tìm theo ID và Type="CUSTOMER" (Chuẩn in hoa)
        Optional<User> userOpt = userRepository.findByRefIdAndRefType(id, "CUSTOMER");

        // Bước B: Nếu không thấy -> Tìm theo Type="Customer" (Title case)
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByRefIdAndRefType(id, "Customer");
        }

        // Bước C: Nếu vẫn không thấy -> Tìm theo Type="customer" (Chữ thường)
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByRefIdAndRefType(id, "customer");
        }

        // Bước D (Phao cứu sinh cuối cùng): Tìm theo EMAIL CŨ
        // Đây là cách chắc chắn nhất vì Customer và User luôn chung Email lúc khởi tạo
        if (userOpt.isEmpty() && oldEmail != null) {
            System.out.println("⚠️ Fallback: Đang tìm User theo email cũ: " + oldEmail);
            userOpt = userRepository.findByEmail(oldEmail);
        }

        // XỬ LÝ UPDATE USER
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("✅ Đã tìm thấy User: " + user.getEmail() + " (ID: " + user.getUserID() + ")");

            boolean isChanged = false;

            // Đồng bộ Tên
            if (updatedCustomer.getName() != null && !updatedCustomer.getName().equals(user.getFullName())) {
                user.setFullName(updatedCustomer.getName());
                isChanged = true;
            }
            // Đồng bộ Email (Lấy email mới từ updatedCustomer)
            if (updatedCustomer.getEmail() != null && !updatedCustomer.getEmail().equals(user.getEmail())) {
                user.setEmail(updatedCustomer.getEmail());
                isChanged = true;
            }
            // Đồng bộ SĐT
            if (updatedCustomer.getPhone() != null && !updatedCustomer.getPhone().equals(user.getPhone())) {
                user.setPhone(updatedCustomer.getPhone());
                isChanged = true;
            }
            // Đồng bộ Password
            if (updatedCustomer.getPassword() != null && !updatedCustomer.getPassword().equals(user.getPassword())) {
                user.setPassword(updatedCustomer.getPassword());
                isChanged = true;
            }

            // Chỉ gọi lệnh Update DB khi thực sự có thay đổi
            if (isChanged) {
                userRepository.save(user);
                System.out.println("🚀 Đồng bộ User thành công!");
            }
        } else {
            // In lỗi ra console để debug nếu vẫn không tìm thấy
            System.err.println("❌ LỖI: Không tìm thấy User nào khớp với Customer ID: " + id + " hoặc Email: " + oldEmail);
        }
        // ==================================================================

        CustomerResponse customerResponse = new CustomerResponse();
        modelMapper.map(updatedCustomer, customerResponse);

        return customerResponse;
    }

    @Transactional
    public void deleteCustomer(Long id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        customer.setStatus(false);

        userRepository.findByRefIdAndRefType(id, "CUSTOMER")
                .ifPresent(user -> {
                    user.setStatus(false);
                    userRepository.save(user);
                });

        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomer(){
        return customerRepository.findByStatus(true)
                .stream()
                .map(customer -> {
                    CustomerResponse response = new CustomerResponse();
                    modelMapper.map(customer, response);
                    return response;
                })
                .toList();
    }
}
