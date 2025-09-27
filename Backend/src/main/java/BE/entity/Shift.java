package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name="shift")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="shift_id")
    private Long shiftID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="service_centerID")
    private ServiceCenter serviceCenter;

    private LocalDate shift_date;

    @Column(name="start_time")
    private LocalTime start_time;

    @Column(name="end_time")
    private LocalTime end_time;

    private String status;
}

