package BE.model.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {
    private Long invoiceID;
    private Long maintenanceId;
    private double totalAmount;
    private LocalDateTime issuedDate;
    private List<InvoiceDetailResponse> invoiceDetails;
    private String status;

    private String customerName;
    private String customerPhone;
    private String vehicleLicensePlate;
    private String vehicleModel;
    private String customerEmail;
    private LocalDateTime startTime; // Ngày nhận
    private LocalDateTime endTime;
}