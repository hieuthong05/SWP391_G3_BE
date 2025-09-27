package BE.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerDTO {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String gender;
    private String address;
    private LocalDate birth;
    private boolean status =true;
}
