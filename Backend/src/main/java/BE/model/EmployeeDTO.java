package BE.model;

import BE.entity.ServiceCenter;
import BE.entity.Shift;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeDTO {

    private String name;
    private String password;
    private String phone;
    private String email;
    private String gender;
    private String role;
    private Long serviceCenter;
    private Long shift;
    private boolean status = true;
    private Double salary;
    private String address;
    private LocalDate birth;

}
