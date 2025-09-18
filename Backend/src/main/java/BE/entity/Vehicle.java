package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Vehicle {

    //Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_ID")
    private Long vehicleID;

    //Foreign Key
    @ManyToOne(fetch = FetchType.LAZY)// 1 cus có Many Vehicles
    @JoinColumn(name="customer_ID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_ID")
    private Orders order;

    //Foreign Key

    @Column(unique = true, name = "license_plate",nullable = false)
    private String licensePlate;

    @Column(name = "VIN",unique = true, length = 17,nullable = false)// Ko dc trùng lặp và có 17 kí tự
    private String vin;

    @Column(nullable = false)
    private String model;

    private Integer year;// Năm sản xuất

    private Integer mileage;// số km đã chạy

    @CreationTimestamp
    @Column(updatable = false, name = "day_Created")
    private LocalDateTime dayCreated;

}
