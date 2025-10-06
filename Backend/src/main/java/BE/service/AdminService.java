package BE.service;

import BE.entity.Admin;
import BE.entity.Shift;
import BE.model.AdminDTO;
import BE.model.response.AdminResponse;
import BE.repository.AdminRepository;
import BE.repository.ShiftRepository;
import BE.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShiftRepository shiftRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public AdminResponse getAdminById(Long id){
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + id));

        AdminResponse adminResponse = new AdminResponse();
        modelMapper.map(admin, adminResponse);

        if (admin.getShift() != null) {
            adminResponse.setShift(admin.getShift().getName());
        }

        return adminResponse;
    }

    @Transactional
    public AdminResponse updateAdmin(Long id, AdminDTO dto) {
        Admin admin = adminRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

        if (adminRepository.findByPhoneAndAdminIDNot(dto.getPhone(),id).isPresent()){
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (adminRepository.findByEmailAndAdminIDNot(dto.getEmail(), id).isPresent()){
            throw new IllegalArgumentException("Email already exists");
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()){
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        modelMapper.map(dto, admin);

        if (dto.getShift() != null) {
            Shift shift = shiftRepository.findById(dto.getShift())
                    .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
            admin.setShift(shift);
        }

        Admin updatedAdmin = adminRepository.save(admin);
        AdminResponse adminResponse = new AdminResponse();
        modelMapper.map(updatedAdmin,adminResponse);

        if (updatedAdmin.getShift() != null) {
            adminResponse.setShift(updatedAdmin.getShift().getName());
        }

        return adminResponse;
    }

    @Transactional
    public void deleteAdmin(Long id){
        Admin admin = adminRepository.findById(id)
                                    .orElseThrow(() -> new EntityNotFoundException("Admin not found with id " + id));
        admin.setStatus(false);

        userRepository.findByRefIdAndRefType(id, "ADMIN")
                .ifPresent(user -> {
                    user.setStatus(false);
                    userRepository.save(user);
                });

        adminRepository.save(admin);
    }

    @Transactional
    public List<AdminResponse> getAllAdmin(){
        return adminRepository.findByStatus(true)
                .stream()
                .map(admin -> {
                    AdminResponse response = new AdminResponse();
                    modelMapper.map(admin, response);
                    return response;
                })
                .toList();
    }

}
