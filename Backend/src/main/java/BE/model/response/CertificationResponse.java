package BE.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationResponse {
    private Long certificationID;
    private String certificateName;
    private String issuedBy;
    private String level;
    private LocalDate issuedDate;
    private LocalDate expirationDate;
    private boolean active;
    private boolean status;
    private Long employeeID;
    private String employeeName;
}
