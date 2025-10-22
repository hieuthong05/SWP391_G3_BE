package BE.model.response;

import lombok.Data;

@Data
public class MaintenanceComponentResponse {
    private Long maintenanceComponentID;
    private Long maintenanceId;
    private Long componentId;
    private String componentName;
    private String componentCode;
    private Double componentPrice;
    private int quantity;
}