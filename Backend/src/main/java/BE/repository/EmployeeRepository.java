package BE.repository;

import BE.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
//    Employee findEmployeeById(Long employeeID);
}
