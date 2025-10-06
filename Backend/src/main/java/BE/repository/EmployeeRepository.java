package BE.repository;

import BE.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
//    Employee findEmployeeById(Long employeeID);
    Optional<Employee> findByPhoneAndEmployeeIDNot(String phone, Long employeeID);
    Optional<Employee> findByEmailAndEmployeeIDNot(String email, Long employeeID);
}
