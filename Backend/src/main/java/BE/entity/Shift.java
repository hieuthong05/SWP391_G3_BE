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

    private LocalDate date;

    @Column(name="start")
    private LocalTime start;

    @Column(name="end")
    private LocalTime end;

    private String status;
}

