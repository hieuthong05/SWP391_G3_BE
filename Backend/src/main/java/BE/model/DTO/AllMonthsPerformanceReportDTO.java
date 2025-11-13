package BE.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllMonthsPerformanceReportDTO {
    private List<MonthlyPerformanceReportDTO> monthlyReports;
    private Integer totalMonths;
    private String periodFrom;
    private String periodTo;
}
