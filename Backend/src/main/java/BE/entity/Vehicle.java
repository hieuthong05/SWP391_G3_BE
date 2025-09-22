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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicleID")
    private Long vehicleID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="customerID", nullable = false)
    private Customer customer;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column(name = "VIN", unique = true, length = 17, nullable = false)
    private String vin;

    @Column(name = "type")
    private String type;

    @Column(name="model", nullable = false)
    private String model;

    private Integer year;
    private Integer mileage;
}
