package BE.model.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceDTO {
    @NotNull(message = "maintenanceId is required")
    private Long maintenanceId;
    private double totalAmount;
}
