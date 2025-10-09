package BE.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class TimeSlotDTO {
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private int remainingSlots;
}
