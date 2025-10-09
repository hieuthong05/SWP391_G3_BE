package BE.model.response;


import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VehicleResponse {
    private Long vehicleID;
    private Long customerID;

    private String licensePlate;
    private String vin;
    private String type;
    private String model;
    private Integer year;
    private Integer mileage;
    private LocalDateTime dayCreated;
    private LocalDate lastMaintenanceDate;
    private Integer lastMaintenanceMileage;
    private boolean status;
}
