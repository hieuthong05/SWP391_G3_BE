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
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_ID")
    private Long quotationID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_ID", nullable = false)
    private Orders orders;

    private double estimatedCost;

    private String status;

    @CreationTimestamp
    @Column(name = "created_day", updatable = false)
    private LocalDateTime createdDay;

    // Relationship Many-to-Many với CheckList
    @ManyToMany
    @JoinTable(
            name = "quotation_checklist",
            joinColumns = @JoinColumn(name = "quotation_ID"),
            inverseJoinColumns = @JoinColumn(name = "checklist_ID")
    )
    private List<CheckList> checkLists = new ArrayList<>();
}
