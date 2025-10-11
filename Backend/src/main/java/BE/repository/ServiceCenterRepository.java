package BE.repository;

import BE.entity.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long> {
    Optional<ServiceCenter> findByServiceCenterIDAndStatus(Long serviceCenterID, String status);
    List<ServiceCenter> findByStatus(String status);
    List<ServiceCenter> findByNameContainingAndStatus(String name, String status);
    List<ServiceCenter> findByLocationAndStatus(String location, String status);
    Optional<ServiceCenter> findByEmail(String email);
    Optional<ServiceCenter> findByPhone(String phone);
    Optional<ServiceCenter> findByEmailAndServiceCenterIDNot(String email, Long serviceCenterID);
    Optional<ServiceCenter> findByPhoneAndServiceCenterIDNot(String phone, Long serviceCenterID);
}
