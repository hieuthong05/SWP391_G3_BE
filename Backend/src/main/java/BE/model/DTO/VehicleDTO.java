package BE.model.DTO;

import BE.entity.Customer;
import BE.entity.Orders;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class VehicleDTO {

    private Long customerId;
    private String licensePlate;
    private String vin;

    private String type;
    private Long modelID;
    private Integer year;
    private Integer mileage;
    private LocalDate lastMaintenanceDate;
    private Integer lastMaintenanceMileage;

    private boolean status;
}
