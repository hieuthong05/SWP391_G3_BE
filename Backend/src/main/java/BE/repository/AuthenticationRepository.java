package BE.repository;

import BE.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationRepository extends JpaRepository<Customer, Long> {

}
