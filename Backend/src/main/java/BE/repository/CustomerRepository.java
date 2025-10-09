package BE.repository;

import BE.entity.Admin;
import BE.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhoneAndCustomerIDNot(String phone, Long customerID);
    Optional<Customer> findByEmailAndCustomerIDNot(String email, Long customerID);
    List<Customer> findByStatus(Boolean status);
}

