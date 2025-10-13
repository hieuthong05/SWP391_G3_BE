package BE.model.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long orderId;
    private String status;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private LocalDateTime orderDate;
    private String serviceCenterName;
    private Long serviceCenterId;
    private String serviceType;
    private List<String> serviceNames;
    private Double totalCost;
    private String paymentMethod;
    private Boolean paymentStatus;
    private String notes;

    // Thông tin customer
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Thông tin vehicle
    private Long vehicleId;
    private String vehiclePlateNumber;
    private String vehicleModel;

    private String message;


}
