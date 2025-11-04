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

    long count();

    // Đếm tổng số order theo tháng trong năm
    @Query("SELECT MONTH(o.orderDate) AS month, COUNT(o) AS total " +
            "FROM Orders o " +
            "WHERE YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(o.orderDate) " +
            "ORDER BY MONTH(o.orderDate)")
    List<Object[]> countOrdersByMonth(int year);

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

//---------------------------------------------BY STATUS----------------------------------------------------

     //* Lấy tất cả bookings theo status với eager loading
     //* Sắp xếp theo orderDate mới nhất (DESC)

    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.services " +
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.vehicle " +
            "LEFT JOIN FETCH o.serviceCenter " +
            "WHERE o.status = :status " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByStatusWithDetails(@Param("status") String status);


     //* Đếm số bookings theo status

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);


     //* Lấy bookings theo nhiều status (Optional - useful cho các trường hợp cần filter nhiều status)

    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.services " +
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.vehicle " +
            "LEFT JOIN FETCH o.serviceCenter " +
            "WHERE o.status IN :statuses " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByStatusInWithDetails(@Param("statuses") List<String> statuses);


//   ---------------------------------------------BY CUSTOMER ID--------------------------------------------------------------------------------------
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.services " +
            "LEFT JOIN FETCH o.vehicle " +
            "LEFT JOIN FETCH o.serviceCenter " +
            "WHERE o.customer.customerID = :customerId " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByCustomerIdWithDetails(@Param("customerId") Long customerId);


//    /**
//     * Lấy bookings theo customer và status
//     */
//    @Query("SELECT DISTINCT o FROM Orders o " +
//            "LEFT JOIN FETCH o.services " +
//            "LEFT JOIN FETCH o.vehicle " +
//            "LEFT JOIN FETCH o.serviceCenter " +
//            "WHERE o.customer.customerID = :customerId " +
//            "AND o.status = :status " +
//            "ORDER BY o.appointmentDate DESC")
//    List<Orders> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
//                                           @Param("status") String status);
//
//    /**
//     * Lấy upcoming bookings (ngày hẹn >= ngày hiện tại và status không phải Completed/Cancelled)
//     */
//    @Query("SELECT DISTINCT o FROM Orders o " +
//            "LEFT JOIN FETCH o.services " +
//            "LEFT JOIN FETCH o.vehicle " +
//            "LEFT JOIN FETCH o.serviceCenter " +
//            "WHERE o.customer.customerID = :customerId " +
//            "AND o.appointmentDate >= :currentDate " +
//            "AND o.status NOT IN ('Completed', 'Cancelled') " +
//            "ORDER BY o.appointmentDate ASC, o.appointmentTime ASC")
//    List<Orders> findUpcomingBookingsByCustomerId(@Param("customerId") Long customerId,
//                                                  @Param("currentDate") LocalDate currentDate);
//
//    /**
//     * Lấy booking history (Completed hoặc Cancelled)
//     */
//    @Query("SELECT DISTINCT o FROM Orders o " +
//            "LEFT JOIN FETCH o.services " +
//            "LEFT JOIN FETCH o.vehicle " +
//            "LEFT JOIN FETCH o.serviceCenter " +
//            "WHERE o.customer.customerID = :customerId " +
//            "AND o.status IN :statuses " +
//            "ORDER BY o.appointmentDate DESC")
//    List<Orders> findByCustomerIdAndStatusIn(@Param("customerId") Long customerId,
//                                             @Param("statuses") List<String> statuses);
//
//    /**
//     * Đếm số bookings của customer
//     */
//    @Query("SELECT COUNT(o) FROM Orders o WHERE o.customer.customerID = :customerId")
//    long countByCustomerId(@Param("customerId") Long customerId);
//
//    /**
//     * Đếm số bookings theo status của customer
//     */
//    @Query("SELECT COUNT(o) FROM Orders o " +
//            "WHERE o.customer.customerID = :customerId " +
//            "AND o.status = :status")
//    long countByCustomerIdAndStatus(@Param("customerId") Long customerId,
//                                    @Param("status") String status);
}
