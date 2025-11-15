package BE.entity;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_ID")
    private Long vehicleID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="customer_ID", nullable = false)
    private Customer customer;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<Orders> orders = new ArrayList<>();

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column(name = "vin", unique = true, length = 17, nullable = false)
    private String vin;

    @Column(name = "type")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_ID", nullable = false)
    private Model model;

    private Integer mileage;

    @CreationTimestamp
    @Column(name="day_created")
    private LocalDateTime dayCreated;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "last_maintenance_mileage")
    private Integer lastMaintenanceMileage;

    @Column(name = "status",columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean status;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
}
