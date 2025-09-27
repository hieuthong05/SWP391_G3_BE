package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="order_id")   // phải mapping đúng với cột trong DB
    private Long orderID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceid")  // tên cột trong DB
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerid")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicleid")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_centerid")
    private ServiceCenter serviceCenter;

    @CreationTimestamp
    @Column(name="order_date", updatable = false)
    private LocalDateTime orderDate;

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
}
