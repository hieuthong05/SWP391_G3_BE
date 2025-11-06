package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serviceID")
    private Long serviceID;

    @Column(name = "service_name", columnDefinition = "TEXT")
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "service_type", columnDefinition = "TEXT")
    private String serviceType;

    @Column(name = "estimated_time", columnDefinition = "TEXT")
    private String estimatedTime;

    private Double price;

    @Column(name = "warranty_peroid")
    private Integer warrantyPeriod;

    @Column(name = "service_status")
    private String serviceStatus;

    @Column(columnDefinition = "TEXT")
    private String component;

    private LocalDateTime date;

    @ManyToMany(mappedBy = "services")
    private List<Orders> orders = new ArrayList<>();

    @ManyToMany(mappedBy = "services")
    private List<ServicePackage> servicePackages;

}
