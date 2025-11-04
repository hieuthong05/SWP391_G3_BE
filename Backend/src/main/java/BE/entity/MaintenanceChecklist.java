package BE.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "maintenance_checklist")
public class MaintenanceChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_checklist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_id", nullable = false)
    private Maintenance maintenance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private CheckList checkList;

    @Column(name = "status", length = 50) // "PASSED", "FAILED", "RECOMMENDED_REPLACEMENT", "NOT_CHECKED"
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

}