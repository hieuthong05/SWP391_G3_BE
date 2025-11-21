package BE.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequest {
    private String certificateName;
    private String issuedBy;
    private String level;
    private LocalDate issuedDate;
    private LocalDate expirationDate;
    private boolean active;
    private Long employeeID;
}
