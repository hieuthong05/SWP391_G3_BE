package BE.model.response;

import lombok.Data;

@Data
public class MaintenanceChecklistResponse {
    private Long id;
    private Long maintenanceId;
    private Long checkListId;
    private String checkListName;
    private String checkListType;
    private String status;
    private String notes;
}