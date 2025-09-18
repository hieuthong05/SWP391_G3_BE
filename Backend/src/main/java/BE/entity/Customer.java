package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="customer_ID")
    private Long customerID;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true)
    private String email;

    private String password;

    private String gender;

    private String address;

    @CreationTimestamp// tự động lấy thời gian
    @Column(updatable = false, name ="day_created")
    private LocalDateTime dayCreated;

    private String phone;

    private boolean status;

}
