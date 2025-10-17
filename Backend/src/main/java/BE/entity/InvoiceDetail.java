package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_detail_ID")
    private Long invoiceDetailID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_ID", nullable = false)
    private Invoice invoice;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "service_ID", nullable = false)
//    private Service service;

    private int quantity;

    private double unitPrice;

    private double subTotal;
}
