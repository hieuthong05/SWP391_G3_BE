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
public class Orders {

    //Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="order_ID")
    private Long orderID;

    //Foreign key
    // One to Many vs Orders table
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    //Foreign key

    private String status;

    @CreationTimestamp
    @Column(name="day_created",updatable = false)
    private LocalDateTime dayCreated;

    private double total;

    @Column(name="payment_Status")
    private String paymentStatus;

    @Column(name="payment_Method")
    private String paymentMethod;
}
