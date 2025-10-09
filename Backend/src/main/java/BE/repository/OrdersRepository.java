package BE.repository;

import BE.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
}
