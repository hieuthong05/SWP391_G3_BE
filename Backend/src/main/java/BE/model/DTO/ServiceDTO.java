package BE.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceDTO {
    private String component;
    private LocalDateTime date;
    private String description;
    private String estimatedTime;
    private Double price;
    private String serviceName;
    private String serviceType;
    private Integer warrantyPeriod;
}
