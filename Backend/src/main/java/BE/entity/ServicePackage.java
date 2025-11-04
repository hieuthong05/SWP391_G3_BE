package BE.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "service_package")
public class ServicePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_ID")
    private Long packageId;

    @Column(nullable = false, unique = true)
    private String name;   // Ví dụ: "Bảo dưỡng định kỳ 5000km"

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết gói dịch vụ

    @Column(name = "interval_km")
    private Integer intervalKm;  // Bao nhiêu km thì cần bảo dưỡng (VD: 5000)

    @Column(name = "interval_months")
    private Integer intervalMonths; // Bao nhiêu tháng thì cần bảo dưỡng (VD: 6)

    private Double price;

    private String status; // active/inactive

    // Một gói định kỳ có thể bao gồm nhiều dịch vụ đơn lẻ
    @ManyToMany
    @JoinTable(
            name = "package_services",
            joinColumns = @JoinColumn(name = "package_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<Service> services;
}
