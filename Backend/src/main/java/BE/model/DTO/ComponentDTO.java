package BE.model.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ComponentDTO {

    private Long serviceCenterID;
    private String name;
    private String code;
    private String type;
    private String description;
    private Double price;
    private Integer quantity;
    private Integer minQuantity;
    private String supplierName;
//    private byte[] image;

    private MultipartFile image;
}
