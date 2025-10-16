package BE.model.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ConfirmBookingResponse {

    // Order info
    private Long orderId;
    private String orderStatus;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    // Maintenance info
    private Long maintenanceId;
    private String maintenanceStatus;
    private LocalDateTime startTime;

    // Customer & Vehicle info
    private String customerName;
    private String vehiclePlateNumber;
    private String vehicleModel;

    // Service Center info
    private String serviceCenterName;

    // Employee info
    private Long employeeId;
    private String employeeName;

    // Services
    private String serviceType;
    private Double totalCost;

    // Message
    private String message;
}
