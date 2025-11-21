package BE.model.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {

    long userID;
    String name;
    String email;
    String gender;
    String phone;
    String role;
    String token;
    private String address;  // Địa chỉ
    private LocalDate birth;

    private Long serviceCenter; // Trả về ID
    private Long shift;
    private Long employeeID;
    private Long customerID;
}
