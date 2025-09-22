package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_ID")
    private Long reportID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_ID", nullable = false)
    private Admin admin;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_day", updatable = false)
    private LocalDateTime createdDay;
}
