package BE.model.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ShiftDTO {

    private Long shiftID;
    private String name;

    private Long serviceCenterID;
    private String serviceCenterName;

    private LocalDate shift_date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime start_time;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime end_time;

    private boolean status;
}