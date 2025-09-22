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
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="orderID")
    private Long orderID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceID")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerID")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicleID")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_centerID")
    private ServiceCenter serviceCenter;

    @CreationTimestamp
    @Column(name="order_date", updatable = false)
    private LocalDateTime orderDate;

    private String status;

    @Column(name="total_cost")
    private Double totalCost;

    @Column(name="paymnent_status")
    private Boolean paymentStatus;

    @Column(name="payment_method")
    private String paymentMethod;

    private String notes;
}
