package BE.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformanceReportDTO {
    private Integer month;
    private Integer year;
    private Long totalMaintenances;
    private List<TechnicianPerformanceDTO> allTechnicians;
    private List<TechnicianPerformanceDTO> top3Performers;
    private List<TechnicianPerformanceDTO> bottom3Performers;
}
