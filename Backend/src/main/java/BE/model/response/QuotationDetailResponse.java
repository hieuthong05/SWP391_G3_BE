package BE.model.response;

import lombok.Data;

@Data
public class QuotationDetailResponse {
    private Long quotationDetailID;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private double subTotal;
}