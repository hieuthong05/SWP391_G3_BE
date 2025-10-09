package BE.model.DTO;

import lombok.Data;

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
