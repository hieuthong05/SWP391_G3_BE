package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_ID")
    private Long invoiceID;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_ID", nullable = false)
//    private Orders orders;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_ID", nullable = false, unique = true)
    private Maintenance maintenance;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> invoiceDetails = new ArrayList<>();

    private double totalAmount;

    @CreationTimestamp
    @Column(name = "issued_date", updatable = false)
    private LocalDateTime issuedDate;

    private String status;
}
