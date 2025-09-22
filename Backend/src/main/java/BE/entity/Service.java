package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serviceID")
    private Long serviceID;

    @Column(name = "service_name")
    private String serviceName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "estimated_time")
    private String estimatedTime;

    private Double price;

    @Column(name = "warranty_peroid")
    private Integer warrantyPeriod;

    @Column(name = "service_status")
    private String serviceStatus;

    private String component;

    private LocalDateTime date;
}
