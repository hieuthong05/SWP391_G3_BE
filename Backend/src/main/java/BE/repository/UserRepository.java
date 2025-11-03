package BE.repository;

import BE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRefIdAndRefType(Long refId, String refType);
    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'customer' AND u.status = true")
    long countActiveCustomers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'staff' AND u.status = true")
    long countActiveStaff();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'technician' AND u.status = true")
    long countActiveTechnicians();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = true")
    long countAllActiveUsers();

    // Đếm số lượng theo role và tháng/năm
    @Query("""
        SELECT FUNCTION('MONTH', u.createdAt) AS month,
               FUNCTION('YEAR', u.createdAt) AS year,
               u.role AS role,
               COUNT(u.userID) AS count
        FROM User u
        WHERE u.status = true
        GROUP BY FUNCTION('YEAR', u.createdAt), FUNCTION('MONTH', u.createdAt), u.role
        ORDER BY year, month
    """)
    List<Object[]> countUsersByRoleAndMonth();
}
