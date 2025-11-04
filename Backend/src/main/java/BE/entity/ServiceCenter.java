package BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "service_center")
public class ServiceCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_center_ID")
    private Long serviceCenterID;

    @Column(columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(columnDefinition = "VARCHAR(500)")
    private String address;

    @Column(columnDefinition = "VARCHAR(100)")
    private String location;

    private String phone;
    private String email;

    @Column(name="open_time")
    private String openTime;

    @Column(name="close_time")
    private String closeTime;

    private String status;
    private String image;

    @OneToMany(mappedBy = "serviceCenter")
    private List<Orders> orders;

    @OneToMany(mappedBy = "serviceCenter")
    private List<Employee> employees;

    @OneToMany(mappedBy = "serviceCenter")
    private List<Component> components;
}
