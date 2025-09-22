package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Revenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revenue_ID")
    private Long revenueID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_ID", nullable = false)
    private Orders orders;

    private double amount;

    @CreationTimestamp
    @Column(name = "recorded_date", updatable = false)
    private LocalDateTime recordedDate;
}
