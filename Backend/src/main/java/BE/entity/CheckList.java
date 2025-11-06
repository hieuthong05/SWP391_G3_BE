package BE.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "checklist")
public class CheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_ID")
    private Long checkListId;

    @Column(name = "checklist_name", columnDefinition = "TEXT")
    private String checkListName;

    @Column(name = "checklist_type", columnDefinition = "TEXT")
    private String checkListType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Relationship Many-to-Many với Quotation
    @ManyToMany(mappedBy = "checkLists")
    private List<Quotation> quotations = new ArrayList<>();

    // Relationship One-to-Many với Component
    // Một CheckList có thể có nhiều Components
    @OneToMany(mappedBy = "checkList", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Component> components = new ArrayList<>();
}