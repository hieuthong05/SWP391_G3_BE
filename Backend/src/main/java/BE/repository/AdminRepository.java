package BE.repository;

import BE.entity.Admin;
import BE.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
//    Admin findAdminById(Long adminID);
}
