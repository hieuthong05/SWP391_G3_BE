package BE.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    private long id;
    private String fullName;
    private String email;
    private String gender;
    private String phone;
    private String role;
    private String token;
}
