package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class QuotationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_detail_ID")
    private Long quotationDetailID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_ID", nullable = false)
    private Quotation quotation;

    @Column(name = "item_name")
    private String itemName;

    private int quantity;

    private double unitPrice;

    private double subTotal;
}