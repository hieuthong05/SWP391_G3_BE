package BE.model.response;

import lombok.Data;

@Data
public class UserResponse {

    long userID;
    String name;
    String email;
    String gender;
    String phone;
    String role;
    String token;
}
