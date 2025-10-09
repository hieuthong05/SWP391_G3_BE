package BE.repository;

import BE.entity.Admin;
import BE.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
//    Admin findAdminById(Long adminID);
    Optional<Admin> findByPhoneAndAdminIDNot(String phone, Long adminID);
    Optional<Admin> findByEmailAndAdminIDNot(String email, Long adminID);
    List<Admin> findByStatus(Boolean status);
}
