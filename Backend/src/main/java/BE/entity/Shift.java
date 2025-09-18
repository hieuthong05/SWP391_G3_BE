package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Shift {

    //Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="shift_ID")
    private Long shiftID;

    //Foreign Key
    //Many to Many Technician

    //

    //Foreign Key

    private LocalDateTime day;

    @Column(name="start_time")
    private LocalDateTime startTime;

    @Column(name="end_time")
    private LocalDateTime endTime;

    private String status;//1: Not yet 2: Working 3:Done

}
