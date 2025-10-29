package BE.model.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ModelDTO {

    private String modelName;
    private MultipartFile imageUrl;
}