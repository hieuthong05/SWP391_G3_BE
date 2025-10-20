package BE.model.response;

import lombok.Data;

@Data
public class InvoiceDetailResponse {
    private Long invoiceDetailID;
    private String itemName; // Tên của dịch vụ/linh kiện
    private int quantity;
    private double unitPrice;
    private double subTotal;
}