package BE.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmBookingRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Technician ID is required")
    private Long technicianId;


    private String maintenanceDescription;
    private String staffNotes;
}
