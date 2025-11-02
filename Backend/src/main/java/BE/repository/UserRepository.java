package BE.repository;

import BE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
