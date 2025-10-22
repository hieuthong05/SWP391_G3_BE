package BE.model.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuotationDTO {
    @NotNull(message = "Maintenance ID is required")
    private Long maintenanceId;

}