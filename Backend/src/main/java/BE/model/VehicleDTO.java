package BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleDTO {

    private long vehicleID;

    @NotNull(message = "Customer ID is required !")
    private long customerID;

    @NotEmpty(message = "License plate is required !")
    private String licensePlate;

    @NotEmpty(message = "VIN is required !")
    @Size(min = 17 ,max=17, message = "VIN must be exactly 17 characters")
    private String vin;

    @NotEmpty(message = "Model is required !")
    private String model;

    @Min(value = 1886, message = "Year must be greater than 1886 !")
    private Integer year;

    @Min(value = 0, message = "Mileage must be non-negative")
    private Integer mileage;


}
