package BE.repository;

import BE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRefIdAndRefType(Long refId, String refType);
    Optional<User> findByEmail(String email);
}
