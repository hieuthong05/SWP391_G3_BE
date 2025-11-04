package BE.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @Column(name="shift_ID")
    private Long shiftID;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="service_centerID")
    private ServiceCenter serviceCenter;

    private LocalDate shift_date;

    @Column(name="start_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime start_time;

    @Column(name="end_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime end_time;

    private boolean status;
}

