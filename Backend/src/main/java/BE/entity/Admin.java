package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_ID")
    private Long adminID;

    private String name;
    private String password;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_center_ID")
    private ServiceCenter serviceCenter;

    private String gender;
    private Double salary;
    private String address;
    private LocalDate birth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift")
    private Shift shift;

    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
