package BE.controller;

import BE.model.DTO.AvailableTimeSlotsDTO;
import BE.model.request.BookingRequest;
import BE.model.response.BookingResponse;
import BE.model.response.CustomerBookingResponse;
import BE.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    @Autowired
    BookingService bookingService;

    @GetMapping("/available-slots")
    public ResponseEntity<AvailableTimeSlotsDTO> getAvailableSlots(
            @RequestParam Long serviceCenterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        AvailableTimeSlotsDTO slots = bookingService.getAvailableTimeSlots(serviceCenterId, date);
        return ResponseEntity.ok(slots);
    }

    @SecurityRequirement(name = "api")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest dto)
    {
        BookingResponse response = bookingService.createBooking(dto);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/customer/{customerId}")
//    public ResponseEntity<List<BookingResponse>> getCustomerBookings(
//            @PathVariable Long customerId)
//    {
//        List<BookingResponse> bookings = bookingService.getCustomerBookings(customerId);
//        return ResponseEntity.ok(bookings);
//    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @PathVariable Long orderId,
            @RequestParam Long customerId)
    {
        bookingService.cancelBooking(orderId, customerId);
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllBookingsPaginated(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "orderDate") String sortBy,
//            @RequestParam(defaultValue = "DESC") String sortDir
    )
    {

        List<BookingResponse> bookings = bookingService.getAllBookings();

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        response.put("totalBookings", bookings.size());

        return ResponseEntity.ok(response);
    }

     //* Lấy tất cả bookings của customer theo customerId
     //* GET /api/bookings/customer/{customerId}

    @SecurityRequirement(name = "api")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> getBookingsByCustomerId(
            @PathVariable Long customerId)
    {
        List<CustomerBookingResponse> bookings = bookingService.getBookingsByCustomerId(customerId);

        Map<String, Object> response = new HashMap<>();

        response.put("bookings", bookings);
        response.put("totalBookings", bookings.size());
        return ResponseEntity.ok(response);
    }
}
