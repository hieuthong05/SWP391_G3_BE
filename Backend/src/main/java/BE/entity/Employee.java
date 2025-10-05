package BE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
    @Column(name = "employee_ID")
    private Long employeeID;

    private String name;
    private String password;

    @Column(unique = true)
    private String phone;

    @Column(unique = true)
    @Email
    private String email;

    private String gender;
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_center_ID")
    private ServiceCenter serviceCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift")
    private Shift shift;

    private boolean status;
    private Double salary;
    private String address;
    private LocalDate birth;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
