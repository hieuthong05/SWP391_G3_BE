package BE.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuotationResponse {
    private Long quotationID;
    private Long maintenanceId;
    private double totalAmount;
    private String status;
    private LocalDateTime createdDate;
    private List<QuotationDetailResponse> quotationDetails;

    //Ngoài table quotation
    private String customerName;
    private String technicianName;
    private String vehicleModel;
    private String vehicleLicensePlate;
    private List<ChecklistItemStatusResponse> checklistItemsStatus; // Danh sách hạng mục checklist và trạng thái của chúng
    private List<MaintenanceComponentResponse> componentsUsed; // Danh sách linh kiện đã sử dụng trong maintenance
}