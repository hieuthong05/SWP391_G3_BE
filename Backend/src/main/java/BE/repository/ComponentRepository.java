package BE.repository;

import BE.entity.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<BE.entity.Component, Long> {
    Optional<BE.entity.Component> findByComponentIDAndStatus(Long componentID, String status);
    List<BE.entity.Component> findByStatus(String status);
    List<BE.entity.Component> findByServiceCenterAndStatus(ServiceCenter serviceCenter, String status);
    List<BE.entity.Component> findByTypeAndStatus(String type, String status);
    List<BE.entity.Component> findByNameContainingAndStatus(String name, String status);
    Optional<BE.entity.Component> findByCode(String code);
    Optional<BE.entity.Component> findByCodeAndComponentIDNot(String code, Long componentID);

    // Query cho low stock components
    @Query("SELECT c FROM Component c WHERE c.status = :status AND c.quantity <= c.minQuantity")
    List<BE.entity.Component> findByStatusAndQuantityLessThanEqualMinQuantity(@Param("status") String status);
}