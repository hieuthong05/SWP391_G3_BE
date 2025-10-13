package BE.model.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class CustomerBookingResponse {
    private Long orderId;
    private String status;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private LocalDateTime orderDate;
    private String serviceCenterName;
    private String serviceType;
    private List<String> serviceNames;
    private Double totalCost;
    private String paymentMethod;
    private Boolean paymentStatus;
    private String notes;

    // Thông tin vehicle
    private Long vehicleId;
    private String vehiclePlateNumber;
    private String vehicleModel;

    private String message;
}
