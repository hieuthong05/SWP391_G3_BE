package BE.model.response;

import lombok.Data;

@Data
public class ComponentResponse {
    private Long componentID;
    private Long serviceCenterID;
    private String serviceCenterName;
    private String name;
    private String code;
    private String type;
    private String description;
    private Double price;
    private Integer quantity;
    private Integer minQuantity;
    private String supplierName;
    private String status;
//    private byte[] image;

    private String imageUrl;
}
