package BE.repository;

import BE.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByServiceIDAndServiceStatus(Long serviceID, String serviceStatus);
    List<Service> findByServiceStatus(String serviceStatus);
    List<Service> findByServiceTypeAndServiceStatus(String serviceType, String serviceStatus);
    List<Service> findByServiceNameContainingAndServiceStatus(String serviceName, String serviceStatus);

    @Query("SELECT s FROM Service s JOIN FETCH s.orders WHERE s.serviceID = :serviceId")
    Optional<Service> findByIdWithOrders(@Param("serviceId") Long serviceId);

    @Query("SELECT s, COUNT(o) as orderCount FROM Service s JOIN s.orders o GROUP BY s ORDER BY orderCount DESC")
    List<Object[]> findMostBookedServices();
}
