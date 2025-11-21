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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    @Autowired
    BookingService bookingService;

    @SecurityRequirement(name = "api")
    @GetMapping("/available-slots")
    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin')")
    public ResponseEntity<AvailableTimeSlotsDTO> getAvailableSlots(
            @RequestParam Long serviceCenterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        AvailableTimeSlotsDTO slots = bookingService.getAvailableTimeSlots(serviceCenterId, date);
        return ResponseEntity.ok(slots);
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin')")
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

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin')")
    @SecurityRequirement(name = "api")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @PathVariable Long orderId,
            @RequestParam Long customerId)
    {
        bookingService.cancelBooking(orderId, customerId, "CUSTOMER");
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
    }

    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @PutMapping("/{orderId}/admin-cancel")
    public ResponseEntity<Map<String, String>> cancelBookingByCenter(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam String role)
    {
        bookingService.cancelBooking(orderId, userId, role.toUpperCase());
        return ResponseEntity.ok(Map.of("message", "Booking cancelled by Admin/Staff successfully"));
    }

    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
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


//     * Lấy tất cả bookings theo status
//     * GET /api/bookings/status/{status}
//     * Example: GET /api/bookings/status/Pending
    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(
            @PathVariable String status)
    {
        List<BookingResponse> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }


     //* Alternative: Lấy bookings theo status qua query parameter
     //* GET /api/bookings/by-status?status=Pending@PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @GetMapping("/by-status")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatusQuery(
            @RequestParam String status)
    {
        List<BookingResponse> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Lấy bookings theo nhiều status
     * GET /api/bookings/by-statuses?statuses=Pending,Confirmed
     */
    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @GetMapping("/by-statuses")
    public ResponseEntity<List<BookingResponse>> getBookingsByMultipleStatus(
            @RequestParam List<String> statuses) {

        List<BookingResponse> bookings = bookingService.getBookingsByMultipleStatus(statuses);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Lấy thống kê số lượng bookings theo từng status
     * GET /api/bookings/status-statistics
     */
    @PreAuthorize("hasAnyAuthority('staff', 'admin')")
    @SecurityRequirement(name = "api")
    @GetMapping("/status-statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatusStatistics() {

        Map<String, Long> statistics = bookingService.getBookingStatusStatistics();

        // Tính tổng
        long totalBookings = statistics.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("statistics", statistics);
        response.put("totalBookings", totalBookings);

        return ResponseEntity.ok(response);
    }


     //* Lấy danh sách tất cả status có sẵn
     //* GET /api/bookings/available-statuses
     @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "api")
    @GetMapping("/available-statuses")
    public ResponseEntity<Map<String, Object>> getAvailableStatuses()
    {

        List<String> statuses = Arrays.asList(
                "Pending",
                "Confirmed",
                "In Progress",
                "Completed",
                "Cancelled"
        );

        Map<String, Object> response = new HashMap<>();
        response.put("availableStatuses", statuses);
        response.put("description", Map.of(
                "Pending", "Booking is waiting for confirmation",
                "Confirmed", "Booking has been confirmed",
                "In Progress", "Service is currently being performed",
                "Completed", "Service has been completed",
                "Cancelled", "Booking has been cancelled"
        ));

        return ResponseEntity.ok(response);
    }

    //-----------------------------------------------------API BY CUSTOMER ID----------------------------------------------------------------

    //* Lấy tất cả bookings của customer theo customerId
     //* GET /api/bookings/customer/{customerId}
    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin')")
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


//    /**
//     * Lấy bookings của customer theo status
//     * GET /api/bookings/customer/{customerId}?status=Pending
//     */
//    @SecurityRequirement(name = "api")
//    @GetMapping("/customer/{customerId}/by-status")
//    public ResponseEntity<List<CustomerBookingResponse>> getBookingsByCustomerIdAndStatus(
//            @PathVariable Long customerId,
//            @RequestParam(required = false) String status) {
//
//        List<CustomerBookingResponse> bookings =
//                bookingService.getBookingsByCustomerIdAndStatus(customerId, status);
//        return ResponseEntity.ok(bookings);
//    }
//
//    /**
//     * Lấy upcoming bookings của customer (chưa hoàn thành)
//     * GET /api/bookings/customer/{customerId}/upcoming
//     */
//    @SecurityRequirement(name = "api")
//    @GetMapping("/customer/{customerId}/upcoming")
//    public ResponseEntity<List<CustomerBookingResponse>> getUpcomingBookings(
//            @PathVariable Long customerId) {
//
//        List<CustomerBookingResponse> bookings =
//                bookingService.getUpcomingBookingsByCustomerId(customerId);
//        return ResponseEntity.ok(bookings);
//    }
//
//    /**
//     * Lấy booking history của customer (đã hoàn thành hoặc đã hủy)
//     * GET /api/bookings/customer/{customerId}/history
//     */
//    @SecurityRequirement(name = "api")
//    @GetMapping("/customer/{customerId}/history")
//    public ResponseEntity<List<CustomerBookingResponse>> getBookingHistory(
//            @PathVariable Long customerId) {
//
//        List<CustomerBookingResponse> bookings =
//                bookingService.getBookingHistoryByCustomerId(customerId);
//        return ResponseEntity.ok(bookings);
//    }
//
//    /**
//     * Lấy thống kê bookings của customer
//     * GET /api/bookings/customer/{customerId}/stats
//     */
//    @SecurityRequirement(name = "api")
//    @GetMapping("/customer/{customerId}/stats")
//    public ResponseEntity<Map<String, Object>> getCustomerBookingStats(
//            @PathVariable Long customerId) {
//
//        long totalBookings = ordersRepository.countByCustomerId(customerId);
//        long pendingBookings = ordersRepository.countByCustomerIdAndStatus(customerId, "Pending");
//        long confirmedBookings = ordersRepository.countByCustomerIdAndStatus(customerId, "Confirmed");
//        long completedBookings = ordersRepository.countByCustomerIdAndStatus(customerId, "Completed");
//        long cancelledBookings = ordersRepository.countByCustomerIdAndStatus(customerId, "Cancelled");
//
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("customerId", customerId);
//        stats.put("totalBookings", totalBookings);
//        stats.put("pendingBookings", pendingBookings);
//        stats.put("confirmedBookings", confirmedBookings);
//        stats.put("completedBookings", completedBookings);
//        stats.put("cancelledBookings", cancelledBookings);
//
//        return ResponseEntity.ok(stats);
//    }
}
