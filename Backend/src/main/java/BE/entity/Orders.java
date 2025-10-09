package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="order_ID")   // phải mapping đúng với cột trong DB
    private Long orderID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_ID")  // tên cột trong DB
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_ID")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_ID")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_center_ID")
    private ServiceCenter serviceCenter;

    // Ngày giờ HẸN (không phải ngày tạo order)
    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    @CreationTimestamp
    @Column(name="order_date", updatable = false)
    private LocalDateTime orderDate;

    // Pending, Confirmed, In Progress, Completed, Cancelled
    private String status;

    @Column(name="total_cost")
    private Double totalCost;

    // trong DB bạn có 2 cột: "payment_status" và "paymnent_status" (có typo)
    // nên phải xác định dùng cái nào, ở đây mình để "payment_status"
    @Column(name="payment_status")
    private Boolean paymentStatus;

    @Column(name="payment_method")
    private String paymentMethod;

    private String notes;

    @ManyToMany
    @JoinTable(
            name = "order_service_package",
            joinColumns = @JoinColumn(name = "order_ID"),
            inverseJoinColumns = @JoinColumn(name = "package_ID")
    )
    private List<ServicePackage> servicePackages = new ArrayList<>();
}
