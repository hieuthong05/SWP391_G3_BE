package BE.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianPerformanceDTO {
    private Long employeeID;
    private String employeeName;
    private String email;
    private String phone;
    private Long maintenanceCount;
}
