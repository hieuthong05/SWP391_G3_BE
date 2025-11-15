package BE.model.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VehicleDTO {

    private Long customerId;
    private String licensePlate;
    private String vin;

    private String type;
    private Long modelID;
    private Integer mileage;
    private LocalDate lastMaintenanceDate;
    private Integer lastMaintenanceMileage;

    private boolean status;
}
