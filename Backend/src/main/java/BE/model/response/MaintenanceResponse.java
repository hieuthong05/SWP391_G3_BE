package BE.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MaintenanceResponse {

    private Long maintenanceID;
    private Long orderID;

    private Long empID;
    private String empName;

    private Long vehicleID;
    private String licensePlate;
    private String model;

    private String description;
    private Double cost;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime nextDueDate;

    private String status;
    private String notes;

    private Long invoiceID;
}
