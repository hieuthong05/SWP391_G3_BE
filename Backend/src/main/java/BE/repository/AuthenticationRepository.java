package BE.repository;

import BE.entity.Customer;
import BE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationRepository extends JpaRepository<User, Long> {

    //Find user by Phone
    User findUserByPhone(String phone);
    //find: tìm; Object: User; where attribute = phone
}
