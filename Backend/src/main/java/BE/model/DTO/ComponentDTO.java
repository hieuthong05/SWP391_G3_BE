package BE.model.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@JsonIgnoreProperties(value = {"image"}, allowGetters = true)
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
