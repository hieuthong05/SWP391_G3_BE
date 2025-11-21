package BE.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "certification")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certification_ID")
    private Long certificationID;

    @Column(nullable = false)
    private String certificateName; // Ví dụ: "High Voltage Safety Level 2"

    private String issuedBy;       // Đơn vị cấp (IMI, VinFast, Toyota...)
    private String level;          // Level 1/2/3 hoặc EV Specialist

    private LocalDate issuedDate;  // Ngày cấp
    private LocalDate expirationDate; // Ngày hết hạn

    private boolean active; // còn hiệu lực hay không
    private boolean status = true; // để soft delete nếu xóa thì status = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_ID")
    private Employee employee;
}
