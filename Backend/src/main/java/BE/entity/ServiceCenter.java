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
    @Column(name = "service_centerID")
    private Long serviceCenterID;

    private String name;
    private String address;
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
