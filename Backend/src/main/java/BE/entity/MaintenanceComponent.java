package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MaintenanceComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_component_ID")
    private Long maintenanceComponentID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_ID", nullable = false)
    private Maintenance maintenance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_ID", nullable = false)
    private Component component;

    private int quantity;
}
