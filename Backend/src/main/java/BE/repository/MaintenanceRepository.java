package BE.repository;

import BE.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    long count();


     //* Kiểm tra xem order đã có maintenance chưa

    @Query("SELECT m FROM Maintenance m WHERE m.orders.orderID = :orderId")
    Optional<Maintenance> findByOrderId(@Param("orderId") Long orderId);


     //* Lấy tất cả maintenance của một employee

    @Query("SELECT m FROM Maintenance m WHERE m.employee.employeeID = :employeeId ORDER BY m.startTime DESC")
    List<Maintenance> findByEmployeeId(@Param("employeeId") Long employeeId);


     //* Lấy tất cả maintenance theo status

    List<Maintenance> findByStatusOrderByStartTimeDesc(String status);


     //* Kiểm tra order đã có maintenance chưa

    boolean existsByOrders_OrderID(Long orderId);
}
