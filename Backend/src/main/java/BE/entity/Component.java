package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "component")
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "component_ID")
    private Long componentID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_centerID")
    private ServiceCenter serviceCenter;

    // Relationship Many-to-One với CheckList
    // Nhiều Components thuộc về một CheckList
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_ID")
    private CheckList checkList;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String name;

    @Column(unique = true)
    private String code;

    private String type;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private Double price;
    private Integer quantity;

    @Column(name = "min_quantity")
    private Integer minQuantity;

    @Column(name = "supplier_name", columnDefinition = "NVARCHAR(MAX)")
    private String supplierName;

//    @Lob
//    private byte[] image;

    @Column(name = "image_url", columnDefinition = "NVARCHAR(500)")
    private String imageUrl; // lưu đường dẫn ảnh

    private String status;
}
