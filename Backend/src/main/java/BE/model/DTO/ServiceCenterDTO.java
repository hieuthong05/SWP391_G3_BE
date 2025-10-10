package BE.model.DTO;

import lombok.Data;

@Data
public class ServiceCenterDTO {
    private Long serviceCenterID;
    private String name;
    private String address;
    private String location;
    private String phone;
    private String email;
    private String openTime;
    private String closeTime;
    private String image;
}
