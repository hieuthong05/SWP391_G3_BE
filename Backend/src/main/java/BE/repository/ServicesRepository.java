package BE.repository;

import BE.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicesRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByServiceIDAndServiceStatus(Long serviceID, String serviceStatus);
    List<Service> findByServiceStatus(String serviceStatus);
    List<Service> findByServiceTypeAndServiceStatus(String serviceType, String serviceStatus);
    List<Service> findByServiceNameContainingAndServiceStatus(String serviceName, String serviceStatus);
}
