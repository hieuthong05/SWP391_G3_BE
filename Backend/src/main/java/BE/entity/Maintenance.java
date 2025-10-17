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
@Table(name = "maintenance")
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_ID")
    private Long maintenanceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_ID")
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_ID")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_ID")
    private Vehicle vehicle;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private Double cost;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "next_due_date")
    private LocalDateTime nextDueDate;

    private String status;

    private String notes;

    @OneToMany(mappedBy = "maintenance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceComponent> maintenanceComponents = new ArrayList<>();

    @OneToOne(mappedBy = "maintenance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Invoice invoice;
}
