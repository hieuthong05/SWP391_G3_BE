package BE.repository;

import BE.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    Optional<Certification> findByCertificationIDAndStatusTrue(Long id);
    List<Certification> findByStatusTrue();
    List<Certification> findByEmployee_EmployeeIDAndStatusTrue(Long employeeID);
    List<Certification> findByLevelAndStatusTrue(String level);
    List<Certification> findByIssuedByAndStatusTrue(String issuedBy);
    List<Certification> findByStatus(boolean status);
    List<Certification> findByActiveAndStatusTrue(boolean active);
    List<Certification> findByIssuedDateAndStatusTrue(LocalDate issuedDate);
    List<Certification> findByExpirationDateAndStatusTrue(LocalDate expirationDate);

    @Query("SELECT c FROM Certification c WHERE c.issuedDate BETWEEN :startDate AND :endDate AND c.status = true")
    List<Certification> findByIssuedDateBetween(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM Certification c WHERE c.expirationDate BETWEEN :startDate AND :endDate AND c.status = true")
    List<Certification> findByExpirationDateBetween(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    List<Certification> findByEmployee_EmployeeIDAndActiveAndStatusTrue(Long employeeID, boolean active);
    List<Certification> findByEmployee_EmployeeIDAndLevelAndStatusTrue(Long employeeID, String level);
}


