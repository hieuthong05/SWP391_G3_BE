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
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeID")
    private Long employeeID;

    private String name;
    private String password;

    @Column(unique = true)
    private String phone;

    @Column(unique = true)
    private String email;

    private String gender;
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_centerID")
    private ServiceCenter serviceCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift")
    private Shift shift;

    private String status;
    private Double salary;
    private String address;
    private LocalDate birth;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
