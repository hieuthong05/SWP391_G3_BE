package SWP391.Backend.Controller;

import BE.controller.BookingController;
import BE.model.request.BookingRequest;
import BE.model.response.BookingResponse;
import BE.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateBooking_Success() {
        // Arrange
        BookingRequest mockRequest = new BookingRequest();
        mockRequest.setCustomerId(1L);
        mockRequest.setVehicleId(2L);
        mockRequest.setServiceCenterId(3L);
        mockRequest.setServiceIds(List.of(10L, 20L));
        mockRequest.setAppointmentDate(LocalDate.now().plusDays(1));
        mockRequest.setAppointmentTime(LocalTime.of(9, 30));
        mockRequest.setPaymentMethod("CASH");
        mockRequest.setNotes("Test booking");

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setOrderId(100L);
        mockResponse.setStatus("Pending");
        mockResponse.setCustomerName("Nguyen Van A");
        mockResponse.setServiceCenterName("EV Care Center");
        mockResponse.setTotalCost(250000.0);
        mockResponse.setPaymentMethod("CASH");

        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<BookingResponse> responseEntity = bookingController.createBooking(mockRequest);

        // Assert
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());

        BookingResponse result = responseEntity.getBody();
        assertEquals(100L, result.getOrderId());
        assertEquals("Pending", result.getStatus());
        assertEquals("Nguyen Van A", result.getCustomerName());
        assertEquals("EV Care Center", result.getServiceCenterName());
        assertEquals(250000.0, result.getTotalCost());
        assertEquals("CASH", result.getPaymentMethod());

        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
    }

    @Test
    void testCreateBooking_ServiceThrowsException() {
        // Arrange
        BookingRequest mockRequest = new BookingRequest();
        mockRequest.setCustomerId(1L);
        mockRequest.setVehicleId(2L);
        mockRequest.setServiceCenterId(3L);
        mockRequest.setServiceIds(List.of(5L));
        mockRequest.setAppointmentDate(LocalDate.now().plusDays(2));
        mockRequest.setAppointmentTime(LocalTime.of(10, 0));
        mockRequest.setPaymentMethod("CASH");

        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act + Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingController.createBooking(mockRequest);
        });

        assertEquals("Service error", exception.getMessage());
        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
    }
}
