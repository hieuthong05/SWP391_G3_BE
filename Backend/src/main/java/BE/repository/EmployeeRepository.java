package BE.repository;

import BE.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
//    Employee findEmployeeById(Long employeeID);
    Optional<Employee> findByPhoneAndEmployeeIDNot(String phone, Long employeeID);
    Optional<Employee> findByEmailAndEmployeeIDNot(String email, Long employeeID);

    List<Employee> findByStatusTrue(); // lấy tất cả employee có status = true


     //* Lấy tất cả employees với eager loading
     //* Tránh N+1 query problem

    @Query("SELECT DISTINCT e FROM Employee e " +
            "LEFT JOIN FETCH e.serviceCenter " +
            "LEFT JOIN FETCH e.shift " +
            "ORDER BY e.createdAt DESC")
    List<Employee> findAllWithDetails();


     //* Lấy employees theo role và status

    @Query("SELECT e FROM Employee e " +
            "LEFT JOIN FETCH e.serviceCenter " +
            "LEFT JOIN FETCH e.shift " +
            "WHERE e.role = :role AND e.status = :status " +
            "ORDER BY e.name ASC")
    List<Employee> findByRoleAndStatus(@Param("role") String role,
                                       @Param("status") boolean status);
}
