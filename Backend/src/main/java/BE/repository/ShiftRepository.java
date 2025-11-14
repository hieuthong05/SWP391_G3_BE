package BE.repository;

import BE.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    @Query("SELECT s FROM Shift s LEFT JOIN FETCH s.serviceCenter")
    List<Shift> findAllWithServiceCenter();
}
