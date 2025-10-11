package BE.model.response;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceResponse {
    private Long serviceID;
    private String component;
    private LocalDateTime date;
    private String description;
    private String estimatedTime;
    private Double price;
    private String serviceName;
    private String serviceType;
    private Integer warrantyPeriod;
}
