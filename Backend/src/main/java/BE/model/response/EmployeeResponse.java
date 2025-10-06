package BE.model.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeResponse {

    private Long employeeID;
    private String name;
    private String phone;
    private String email;
    private String gender;
    private String role;
    private Double salary;
    private String address;
    private LocalDate birth;



    private String serviceCenterName;
    private String shiftName;

}
