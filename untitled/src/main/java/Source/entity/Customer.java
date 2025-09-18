package Source.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long customerID;

    String fullName;

    String email;

    String gender;

    String password;

    Date date;

    @Pattern(
            regexp = "^(03|05|07|08|09|012|016|018|019)[0-9]{8}$",
            message = "Phone invalid!"
    )
    String phone;

    boolean Status;

}
