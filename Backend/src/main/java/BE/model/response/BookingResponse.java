package BE.model.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingResponse {
    private Long orderId;
    private String status;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private LocalDateTime orderDate;
    private String serviceCenterName;
    private String serviceType;  // Service name or Package name
    private Double estimatedCost;
    private String paymentMethod;
    private Boolean paymentStatus;
    private String message;
}
