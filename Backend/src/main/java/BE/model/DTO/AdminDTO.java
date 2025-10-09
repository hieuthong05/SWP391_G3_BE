package BE.model.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class AdminDTO {

    private String address;
    private LocalDate birth;
    private LocalDateTime createdAt;
    private String email;
    private String gender;
    private String name;
    private String password;
    private String phone;
    private String role;
    private BigDecimal salary;
    private Boolean status;
    private Long serviceCenter;
    private String shift;
}