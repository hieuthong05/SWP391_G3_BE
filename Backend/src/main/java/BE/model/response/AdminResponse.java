package BE.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminResponse {
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
    private String shift;
}
