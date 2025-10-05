package BE.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_shift")
@Data
public class EmployeeShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_ID")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "shift_ID")
    private Shift shift;
}

