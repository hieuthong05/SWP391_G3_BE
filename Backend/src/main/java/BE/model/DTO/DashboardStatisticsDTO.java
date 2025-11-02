package BE.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStatisticsDTO {

    private long totalCustomers;
    private long totalStaff;
    private long totalTechnicians;
    private long totalActiveUsers;
    private long totalActiveVehicles;
    private long totalOrders;
    private long totalMaintenances;
}
