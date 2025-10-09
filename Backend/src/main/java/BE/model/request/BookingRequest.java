package BE.model.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Service Center ID is required")
    private Long serviceCenterId;

    // Chọn 1 trong 2
    private Long serviceId;  // Dịch vụ đơn lẻ
    private Long packageId;  // Gói dịch vụ

    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    private LocalTime appointmentTime;

    @NotNull(message = "Payment Method is required")
    private String paymentMethod;

    private String notes;
}
