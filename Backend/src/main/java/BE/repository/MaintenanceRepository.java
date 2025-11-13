package BE.repository;

import BE.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    long count();

    // Đếm tổng số maintenance có order theo tháng
    @Query("SELECT MONTH(m.orders.orderDate) AS month, COUNT(m) AS total " +
            "FROM Maintenance m " +
            "WHERE YEAR(m.orders.orderDate) = :year AND m.orders IS NOT NULL " +
            "GROUP BY MONTH(m.orders.orderDate) " +
            "ORDER BY MONTH(m.orders.orderDate)")
    List<Object[]> countMaintenanceByMonth(int year);


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

    //==============================STATISTIC======================================


    @Query("SELECT m FROM Maintenance m " +
            "WHERE m.employee.role = 'technician' " +
            "AND YEAR(m.endTime) = :year " +
            "AND MONTH(m.endTime) = :month " +
            "AND m.endTime IS NOT NULL")
    List<Maintenance> findByTechnicianAndMonthYear(
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("SELECT DISTINCT YEAR(m.endTime) as year, MONTH(m.endTime) as month " +
            "FROM Maintenance m " +
            "WHERE m.employee.role = 'technician' " +
            "AND m.endTime IS NOT NULL " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> findDistinctMonthsAndYears();

    @Query("SELECT m FROM Maintenance m " +
            "WHERE m.employee.role = 'technician' " +
            "AND m.endTime IS NOT NULL")
    List<Maintenance> findAllByTechnician();
}
