package BE.model.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerDTO {
    private String name;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(unique = true, nullable = false)
    private String phone;

    private String password;
    private String gender;
    private String address;
    private LocalDate birth;
    private boolean status =true;
}
