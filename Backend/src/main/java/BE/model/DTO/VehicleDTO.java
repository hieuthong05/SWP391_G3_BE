package BE.model.DTO;

import BE.entity.Customer;
import BE.entity.Orders;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    private String vin;

    private String type;

    // ✅ Model ID (không phải nested object)
    @NotNull(message = "Model ID is required")
    @Positive(message = "Model ID must be positive")
    private Long modelId;

    @Min(value = 1900, message = "Year must be after 1900")
    @Max(value = 2100, message = "Year must be before 2100")
    private Integer year;

    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileage;

    private Boolean status = true;
}
