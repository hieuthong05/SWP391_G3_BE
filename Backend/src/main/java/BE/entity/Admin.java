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

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(unique = true, columnDefinition = "TEXT")
    private String email;

    @Column(unique = true, columnDefinition = "TEXT")
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String role;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "service_center_ID",nullable = true)
//    private ServiceCenter serviceCenter;

    @Column(columnDefinition = "TEXT")
    private String gender;

    private Double salary;

    @Column(columnDefinition = "TEXT")
    private String address;

    private LocalDate birth;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "shift",nullable = true)
//    private Shift shift;

    private boolean status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
