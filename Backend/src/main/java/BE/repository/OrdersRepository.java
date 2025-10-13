package BE.repository;

import BE.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    // Đếm số booking trong time slot
    @Query("SELECT COUNT(o) FROM Orders o WHERE o.serviceCenter.serviceCenterID = :centerId " +
            "AND o.appointmentDate = :date " +
            "AND CAST(o.appointmentTime AS time) = CAST(:time AS time) " +
            "AND o.status NOT IN ('Cancelled')")
    int countBookingsInTimeSlot(Long centerId, LocalDate date, LocalTime time);

    // Lấy tất cả bookings của customer
    List<Orders> findByCustomer_CustomerIDOrderByAppointmentDateDesc(Long customerId);

    // Lấy bookings của service center theo ngày
    List<Orders> findByServiceCenter_ServiceCenterIDAndAppointmentDate(Long centerId, LocalDate date);

    @Query("SELECT o FROM Orders o JOIN FETCH o.services WHERE o.orderID = :orderId")
    Optional<Orders> findByIdWithServices(@Param("orderId") Long orderId);

    @Query("SELECT DISTINCT o FROM Orders o JOIN o.services s WHERE s.serviceName = :serviceName")
    List<Orders> findOrdersByServiceName(@Param("serviceName") String serviceName);

    @Query("SELECT DISTINCT o FROM Orders o JOIN o.services s WHERE s.serviceID = :serviceId")
    List<Orders> findOrdersByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT o FROM Orders o " +
            "LEFT JOIN FETCH o.services " +
//            "LEFT JOIN FETCH o.servicePackages " +
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.vehicle " +
            "LEFT JOIN FETCH o.serviceCenter " +
            "WHERE o.orderID = :orderId")
    Optional<Orders> findByIdWithAllDetails(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Orders o JOIN o.services s WHERE s.serviceID IN :serviceIds GROUP BY o HAVING COUNT(s) = :count")
    List<Orders> findOrdersWithAllServices(@Param("serviceIds") List<Long> serviceIds,
                                           @Param("count") Long count);

    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.services " +
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.vehicle " +
            "LEFT JOIN FETCH o.serviceCenter")
    List<Orders> findAllWithDetails();

    // Hoặc với ordering
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.services " +
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.vehicle " +
            "LEFT JOIN FETCH o.serviceCenter " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findAllWithDetailsOrderByDate();
}
