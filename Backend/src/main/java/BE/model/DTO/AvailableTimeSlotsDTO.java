package BE.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

    @Data
    @AllArgsConstructor
    public class AvailableTimeSlotsDTO {
        private LocalDate date;
        private List<TimeSlotDTO> timeSlots;
    }
