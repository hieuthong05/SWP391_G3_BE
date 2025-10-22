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
}