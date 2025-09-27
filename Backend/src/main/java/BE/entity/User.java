package BE.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Table(name = "user_account") // bảng login chung
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userID;

    @Column(unique = true, nullable = false)
    private String phone;  // email hoặc username

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;      // CUSTOMER, STAFF, TECHNICIAN, SUPER_ADMIN, BRANCH_ADMIN

    @Column(name = "ref_id", nullable = false)
    private Long refId;       // id của bảng gốc

    @Column(name = "ref_type", nullable = false)
    private String refType;   // CUSTOMER, EMPLOYEE, ADMIN

    private Boolean status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role));
    }

    @Override
    public String getUsername() {
        return this.getPhone();
    }
}
