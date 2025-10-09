package BE.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive number")
    private Long customerId;

    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be a positive number")
    private Long vehicleId;

    @NotNull(message = "Service Center ID is required")
    @Positive(message = "Service Center ID must be a positive number")
    private Long serviceCenterId;

    // Chọn 1 trong 2
    @Positive(message = "Service ID must be a positive number")
    private Long serviceId;  // Dịch vụ đơn lẻ

    @Positive(message = "Package ID must be a positive number")
    private Long packageId;  // Gói dịch vụ

    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    private LocalTime appointmentTime;

    @NotBlank(message = "Payment method is required")
    @Pattern(
            regexp = "^(CASH|CREDIT_CARD|DEBIT_CARD|BANK_TRANSFER|E_WALLET)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Payment method must be one of: CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, E_WALLET"
    )
    private String paymentMethod;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
