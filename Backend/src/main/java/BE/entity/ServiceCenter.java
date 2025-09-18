package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ServiceCenter {

    //Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_center_ID")
    private Long serviceCenterID;

    //Foreign key
    //1-1 vs Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id",nullable = false)
    private Orders orders;
    //n-n vs Technician, Admin, Staff

    //Foreign key

    @Column(name="service_name")
    private String serviceName;

    private String address;

    private String phone;

    private String email;

    @Column(name = "opened_time")
    private LocalDateTime openedTime;

    @Column(name="created_day")
    private LocalDateTime createdDay;

    private boolean status;
}
