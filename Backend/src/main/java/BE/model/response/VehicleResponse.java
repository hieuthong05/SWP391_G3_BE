package BE.model.response;


import BE.entity.Model;
import BE.model.DTO.ModelDTO;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VehicleResponse {
    private Long vehicleID;
    private Long customerID;
    private String customerName;
    private String licensePlate;
    private String vin;
    private String type;
    private ModelResponse model;
    private Integer year;
    private Integer mileage;
    private LocalDateTime dayCreated;
    private LocalDate lastMaintenanceDate;
    private Integer lastMaintenanceMileage;
    private boolean status;
}
