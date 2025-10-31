package SWP391.Backend.Service;

import BE.entity.*;
import BE.model.request.BookingRequest;
import BE.model.response.BookingResponse;
import BE.repository.*;
import BE.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    // Dependencies (Mocks)
    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ServiceCenterRepository serviceCenterRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private ModelMapper modelMapper;


    @InjectMocks // Tự động inject các mock vào BookingService
    private BookingService bookingService;

    private static final Long VALID_CUSTOMER_ID = 1L;
    private static final Long VALID_VEHICLE_ID = 2L;
    private static final Long VALID_SERVICE_CENTER_ID = 3L;
    private static final Long VALID_SERVICE_ID_1 = 10L;
    private static final Long VALID_SERVICE_ID_2 = 20L;
    private static final Long NON_EXISTENT_CUSTOMER_ID = 99L;
    private static final Long NON_EXISTENT_SERVICE_ID = 96L;
    private static final Long OTHER_CUSTOMER_ID = 5L; // ID của khách hàng khác
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(20, 0);
    private static final LocalTime VALID_APPOINTMENT_TIME = LocalTime.of(9, 30);
    private static final LocalTime INVALID_TIME_BEFORE_OPENING = LocalTime.of(7, 59);
    private static final LocalTime INVALID_TIME_AFTER_CLOSING = LocalTime.of(19, 1);
    private static final LocalDate VALID_APPOINTMENT_DATE = LocalDate.now().plusDays(1);
    private static final int MAX_BOOKINGS_PER_SLOT = 8;

    // --- Test Data ---
    private BookingRequest validRequest;
    private Customer validCustomer;
    private Vehicle validVehicle;
    private Vehicle otherCustomerVehicle; // Xe của khách hàng khác
    private ServiceCenter validServiceCenter;
    private BE.entity.Service validService1;
    private BE.entity.Service validService2;
    private Orders mockSavedOrder;
    private BookingResponse mockBookingResponse;

    @BeforeEach
    void setUp() {
        validCustomer = new Customer();
        validCustomer.setCustomerID(VALID_CUSTOMER_ID);
        validCustomer.setName("John Doe");
        validCustomer.setPhone("0901234567");
        validCustomer.setEmail("john@example.com");

        Customer otherCustomer = new Customer();
        otherCustomer.setCustomerID(OTHER_CUSTOMER_ID);

        Model vehicleModel = new Model();
        vehicleModel.setModelID(1L);
        vehicleModel.setModelName("Honda City");

        validVehicle = new Vehicle();
        validVehicle.setVehicleID(VALID_VEHICLE_ID);
        validVehicle.setLicensePlate("59A-12345");
        validVehicle.setCustomer(validCustomer);
        validVehicle.setModel(vehicleModel);

        otherCustomerVehicle = new Vehicle();
        otherCustomerVehicle.setVehicleID(99L);
        otherCustomerVehicle.setLicensePlate("51B-67890");
        otherCustomerVehicle.setCustomer(otherCustomer);
        otherCustomerVehicle.setModel(vehicleModel);

        validServiceCenter = new ServiceCenter();
        validServiceCenter.setServiceCenterID(VALID_SERVICE_CENTER_ID);
        validServiceCenter.setName("Service Center Downtown");

        validService1 = new BE.entity.Service();
        validService1.setServiceID(VALID_SERVICE_ID_1);
        validService1.setServiceName("Oil Change");
        validService1.setPrice(500000.0);

        validService2 = new BE.entity.Service();
        validService2.setServiceID(VALID_SERVICE_ID_2);
        validService2.setServiceName("Tire Rotation");
        validService2.setPrice(300000.0);

        // --- Prepare valid request ---
        validRequest = new BookingRequest();
        validRequest.setCustomerId(VALID_CUSTOMER_ID);
        validRequest.setVehicleId(VALID_VEHICLE_ID);
        validRequest.setServiceCenterId(VALID_SERVICE_CENTER_ID);
        validRequest.setServiceIds(List.of(VALID_SERVICE_ID_1, VALID_SERVICE_ID_2));
        validRequest.setAppointmentDate(VALID_APPOINTMENT_DATE);
        validRequest.setAppointmentTime(VALID_APPOINTMENT_TIME);
        validRequest.setPaymentMethod("CASH");
        validRequest.setNotes("Regular maintenance");

        // --- Prepare mock saved order ---
        mockSavedOrder = new Orders();
        mockSavedOrder.setOrderID(100L);
        mockSavedOrder.setCustomer(validCustomer);
        mockSavedOrder.setVehicle(validVehicle);
        mockSavedOrder.setServiceCenter(validServiceCenter);
        mockSavedOrder.setServices(List.of(validService1, validService2));
        mockSavedOrder.setAppointmentDate(VALID_APPOINTMENT_DATE);
        mockSavedOrder.setAppointmentTime(VALID_APPOINTMENT_TIME);
        mockSavedOrder.setStatus("Pending"); // Status ban đầu
        mockSavedOrder.setPaymentStatus(false);
        mockSavedOrder.setPaymentMethod("CASH");
        mockSavedOrder.setTotalCost(800000.0); // 500k + 300k
        mockSavedOrder.setNotes("Regular maintenance");

        // --- Prepare mock response ---
        mockBookingResponse = new BookingResponse();
        mockBookingResponse.setOrderId(100L);
        mockBookingResponse.setStatus("Pending");
        mockBookingResponse.setCustomerName("John Doe");
        mockBookingResponse.setServiceCenterName("Service Center Downtown");
        mockBookingResponse.setServiceType("Oil Change, Tire Rotation");
        mockBookingResponse.setTotalCost(800000.0);
        mockBookingResponse.setPaymentMethod("CASH");
        mockBookingResponse.setMessage("Booking created successfully with 2 service(s). We will confirm your appointment shortly.");
    }

    // BLACK BOX TESTS (EP & BVA)

    @Test
    @DisplayName("EP-001: Valid booking with services - Success")
    void testCreateBooking_ValidRequest_Success() {
        // Arrange (Mock repository calls)
        //Validate entities exist (Trả về Optional chứa entity hợp lệ)
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));

        //  Check time slot availability (Trả về số lượng booking < MAX)
        when(ordersRepository.countBookingsInTimeSlot(VALID_SERVICE_CENTER_ID, VALID_APPOINTMENT_DATE, VALID_APPOINTMENT_TIME))
                .thenReturn(MAX_BOOKINGS_PER_SLOT - 1); // Còn slot

        // Save order
        // Dùng any(Orders.class) vì đối tượng order được tạo mới bên trong method
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockSavedOrder);

        // Build response using ModelMapper
        // Mock việc map từ savedOrder sang BookingResponse
        when(modelMapper.map(any(Orders.class), eq(BookingResponse.class))).thenReturn(mockBookingResponse);

        // Act
        BookingResponse response = bookingService.createBooking(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
        assertEquals("Pending", response.getStatus());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("Service Center Downtown", response.getServiceCenterName());
        assertEquals("Oil Change, Tire Rotation", response.getServiceType());
        assertEquals(800000.0, response.getTotalCost());
        assertEquals("CASH", response.getPaymentMethod());
        assertTrue(response.getMessage().startsWith("Booking created successfully"));

        // Verify repository calls
        verify(customerRepository, times(1)).findById(VALID_CUSTOMER_ID);
        verify(vehicleRepository, times(1)).findById(VALID_VEHICLE_ID);
        verify(serviceCenterRepository, times(1)).findById(VALID_SERVICE_CENTER_ID);
        verify(serviceRepository, times(1)).findById(VALID_SERVICE_ID_1);
        verify(serviceRepository, times(1)).findById(VALID_SERVICE_ID_2);
        verify(ordersRepository, times(1)).countBookingsInTimeSlot(VALID_SERVICE_CENTER_ID, VALID_APPOINTMENT_DATE, VALID_APPOINTMENT_TIME);
        verify(ordersRepository, times(1)).save(any(Orders.class));
        verify(modelMapper, times(1)).map(any(Orders.class), eq(BookingResponse.class));
        // Kiểm tra modelMapper.map(dto, order)
        verify(modelMapper, times(1)).map(eq(validRequest), any(Orders.class));
    }

    @Test
    @DisplayName("EP-002: Empty/Null service list - Throws exception")
    void testCreateBooking_EmptyServiceList_ThrowsException() {
        //Arrange
        validRequest.setServiceIds(null); // EP: null serviceIds


        //Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Please select at least 1 service"));

        // Verify không có lệnh save nào được gọi
        verify(ordersRepository, never()).save(any(Orders.class));
    }

    @Test
    @DisplayName("EP-003: Invalid entity IDs - Throws exception")
    void testCreateBooking_InvalidCustomerId_ThrowsException() {
        // Arrange
        validRequest.setCustomerId(NON_EXISTENT_CUSTOMER_ID); // EP: customerId không tồn tại

        // Mock repository trả về Optional rỗng
        when(customerRepository.findById(NON_EXISTENT_CUSTOMER_ID)).thenReturn(Optional.empty());
        // Mock các repo khác trả về hợp lệ (để lỗi chỉ do customerId)
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(ordersRepository, never()).save(any(Orders.class));
    }


    @Test
    @DisplayName("EP-004: Invalid Service ID - Throws exception")
    void testCreateBooking_InvalidServiceId_ThrowsException() {
        validRequest.setServiceIds(List.of(VALID_SERVICE_ID_1, NON_EXISTENT_SERVICE_ID)); // EP: 1 ID hợp lệ, 1 ID không tồn tại

        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(NON_EXISTENT_SERVICE_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Service not found"));
        verify(ordersRepository, never()).save(any(Orders.class));
    }

    @Test
    @DisplayName("EP-005: Appointment time outside working hours (before) - Throws exception")
    void testCreateBooking_TimeBeforeOpening_ThrowsException() {
        validRequest.setAppointmentTime(INVALID_TIME_BEFORE_OPENING); // EP: Thời gian trước giờ mở cửa

        // Mock repo (không cần mock countBookingsInTimeSlot vì lỗi thời gian sẽ xảy ra trước)
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Appointment time must be between"));
        verify(ordersRepository, never()).save(any(Orders.class));
    }

    @Test
    @DisplayName("EP-006: Appointment time outside working hours (after) - Throws exception")
    void testCreateBooking_TimeAfterClosing_ThrowsException() {
        validRequest.setAppointmentTime(INVALID_TIME_AFTER_CLOSING); // EP: Thời gian sau giờ đóng cửa (biên cuối)

        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Appointment time must be between"));
        verify(ordersRepository, never()).save(any(Orders.class));
    }

    @Test
    @DisplayName("EP-007: Time slot fully booked - Throws exception")
    void testCreateBooking_SlotFull_ThrowsException() {
        // Mock repo trả về hợp lệ
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));
        when(ordersRepository.countBookingsInTimeSlot(VALID_SERVICE_CENTER_ID, VALID_APPOINTMENT_DATE, VALID_APPOINTMENT_TIME))
                .thenReturn(MAX_BOOKINGS_PER_SLOT); // EP: Slot đầy

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Time slot is fully booked"));
        verify(ordersRepository, never()).save(any(Orders.class));
    }

    @Test
    @DisplayName("BVA-001: Opening time boundary (8:00 valid, 7:59 invalid)")
    void testCreateBooking_OpeningTimeBoundary() {
        // --- Arrange ---
        // Mock repo cho trường hợp hợp lệ
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));
        when(ordersRepository.countBookingsInTimeSlot(anyLong(), any(LocalDate.class), any(LocalTime.class))).thenReturn(0);
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockSavedOrder);
        when(modelMapper.map(any(Orders.class), eq(BookingResponse.class))).thenReturn(mockBookingResponse);

        // Act & Assert: Test 8:00 (Valid)
        validRequest.setAppointmentTime(OPENING_TIME); // BVA: Đúng biên dưới
        assertDoesNotThrow(() -> {
            bookingService.createBooking(validRequest);
        }, "Booking at opening time should be valid");

        //Act & Assert: Test 7:59 (Invalid)
        validRequest.setAppointmentTime(INVALID_TIME_BEFORE_OPENING); // BVA: Ngay dưới biên dưới
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });
        assertTrue(exception.getMessage().contains("Appointment time must be between"));
    }

    @Test
    @DisplayName("BVA-002: Closing time boundary (19:00 valid, 19:01 invalid)")
    void testCreateBooking_ClosingTimeBoundary() {
        // Arrange
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));
        when(ordersRepository.countBookingsInTimeSlot(anyLong(), any(LocalDate.class), any(LocalTime.class))).thenReturn(0);
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockSavedOrder);
        when(modelMapper.map(any(Orders.class), eq(BookingResponse.class))).thenReturn(mockBookingResponse);

        // Act & Assert: Test 19:00 (Valid)
        LocalTime lastValidSlotStart = CLOSING_TIME.minusHours(1); // Slot cuối cùng bắt đầu lúc 19:00
        validRequest.setAppointmentTime(lastValidSlotStart); // BVA: Đúng biên trên
        assertDoesNotThrow(() -> {
            bookingService.createBooking(validRequest);
        }, "Booking at last valid slot start time should be valid");

        //Act & Assert: Test 19:01 (Invalid)
        validRequest.setAppointmentTime(INVALID_TIME_AFTER_CLOSING); // BVA: Ngay trên biên trên
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });
        assertTrue(exception.getMessage().contains("Appointment time must be between"));
    }

    @Test
    @DisplayName("BVA-003: Booking slot boundary (7 bookings valid, 8 full)")
    void testCreateBooking_BookingSlotBoundary() {
        // Arrange (Tương tự test trên)
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockSavedOrder);
        when(modelMapper.map(any(Orders.class), eq(BookingResponse.class))).thenReturn(mockBookingResponse);

        // Act & Assert: Test 7 bookings (Valid)
        when(ordersRepository.countBookingsInTimeSlot(VALID_SERVICE_CENTER_ID, VALID_APPOINTMENT_DATE, VALID_APPOINTMENT_TIME))
                .thenReturn(MAX_BOOKINGS_PER_SLOT - 1); // BVA: Ngay dưới biên giới hạn slot
        assertDoesNotThrow(() -> {
            bookingService.createBooking(validRequest);
        }, "Booking when slot has MAX-1 bookings should be valid");

        // Act & Assert: Test 8 bookings (Full/Invalid)
        when(ordersRepository.countBookingsInTimeSlot(VALID_SERVICE_CENTER_ID, VALID_APPOINTMENT_DATE, VALID_APPOINTMENT_TIME))
                .thenReturn(MAX_BOOKINGS_PER_SLOT); // BVA: Đúng biên giới hạn slot
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });
        assertTrue(exception.getMessage().contains("Time slot is fully booked"));
    }

    // == WHITE BOX TESTS (Decision Coverage)

    @Test
    @DisplayName("WB-001: Decision - Vehicle belongs to customer (true/false)")
    void testCreateBooking_VehicleOwnershipDecision() {
        // Arrange
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));
        when(ordersRepository.countBookingsInTimeSlot(anyLong(), any(LocalDate.class), any(LocalTime.class))).thenReturn(0);

        // Test Case 1: if (!vehicle.getCustomer()... ) is FALSE (Xe thuộc về khách hàng)
        validRequest.setVehicleId(VALID_VEHICLE_ID);
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockSavedOrder); // Chỉ mock save cho case thành công
        when(modelMapper.map(any(Orders.class), eq(BookingResponse.class))).thenReturn(mockBookingResponse); // Chỉ mock map cho case thành công
        assertDoesNotThrow(() -> {
            bookingService.createBooking(validRequest);
        }, "Should pass when vehicle belongs to customer");
        verify(ordersRepository, times(1)).save(any(Orders.class)); // Verify save được gọi

        // Test Case 2: if (!vehicle.getCustomer()... ) is TRUE (Xe KHÔNG thuộc về khách hàng)
        reset(ordersRepository); // Reset mock ordersRepository để kiểm tra never()
        validRequest.setVehicleId(otherCustomerVehicle.getVehicleID());
        when(vehicleRepository.findById(otherCustomerVehicle.getVehicleID())).thenReturn(Optional.of(otherCustomerVehicle)); // Trả về xe của KH khác
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });
        assertTrue(exception.getMessage().contains("Vehicle does not belong to this customer"));
        verify(ordersRepository, never()).save(any(Orders.class)); // Verify save KHÔNG được gọi
    }

    @Test
    @DisplayName("WB-002: Decision - Errors list empty/not empty")
    void testCreateBooking_ErrorsListDecision() {

        // --- Test Case : if (!errors.isEmpty()) is TRUE (Có lỗi)
        validRequest.setCustomerId(NON_EXISTENT_CUSTOMER_ID);
        when(customerRepository.findById(NON_EXISTENT_CUSTOMER_ID)).thenReturn(Optional.empty());
        // Mock các repo khác nếu cần để cô lập lỗi
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(validRequest);
        });
        assertFalse(exception.getMessage().isEmpty()); // Đảm bảo có message lỗi
        verify(ordersRepository, never()).save(any(Orders.class)); // Verify save KHÔNG được gọi
    }

    @Test
    @DisplayName("WB-003: Loop - Multiple services iteration")
    void testCreateBooking_MultipleServicesLoop() {
        // Arrange
        // Mock repo
        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1));
        when(serviceRepository.findById(VALID_SERVICE_ID_2)).thenReturn(Optional.of(validService2));
        when(ordersRepository.countBookingsInTimeSlot(anyLong(), any(LocalDate.class), any(LocalTime.class))).thenReturn(0);
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockSavedOrder);
        when(modelMapper.map(any(Orders.class), eq(BookingResponse.class))).thenReturn(mockBookingResponse);

        // Act
        BookingResponse response = bookingService.createBooking(validRequest);

        // Assert
        assertNotNull(response);
        // Kiểm tra xem cả 2 service đã được tính vào giá và tên
        assertEquals(validService1.getPrice() + validService2.getPrice(), response.getTotalCost());
        assertTrue(response.getServiceType().contains(validService1.getServiceName()));
        assertTrue(response.getServiceType().contains(validService2.getServiceName()));

        // Verify serviceRepository.findById được gọi 2 lần
        verify(serviceRepository, times(2)).findById(anyLong());
        verify(serviceRepository, times(1)).findById(VALID_SERVICE_ID_1);
        verify(serviceRepository, times(1)).findById(VALID_SERVICE_ID_2);
    }

    @Test
    @DisplayName("WB-004: Loop - Single service iteration")
    void testCreateBooking_SingleServiceLoop() {
        // --- Arrange ---
        validRequest.setServiceIds(List.of(VALID_SERVICE_ID_1));
        mockSavedOrder.setServices(List.of(validService1));
        mockSavedOrder.setTotalCost(validService1.getPrice());
        mockBookingResponse.setServiceType(validService1.getServiceName());
        mockBookingResponse.setTotalCost(validService1.getPrice());
        mockBookingResponse.setMessage("Booking created successfully with 1 service(s). We will confirm your appointment shortly.");

        when(customerRepository.findById(VALID_CUSTOMER_ID)).thenReturn(Optional.of(validCustomer));
        when(vehicleRepository.findById(VALID_VEHICLE_ID)).thenReturn(Optional.of(validVehicle));
        when(serviceCenterRepository.findById(VALID_SERVICE_CENTER_ID)).thenReturn(Optional.of(validServiceCenter));
        when(serviceRepository.findById(VALID_SERVICE_ID_1)).thenReturn(Optional.of(validService1)); // Chỉ cần mock service 1
        when(ordersRepository.countBookingsInTimeSlot(anyLong(), any(LocalDate.class), any(LocalTime.class))).thenReturn(0);
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockSavedOrder);
        when(modelMapper.map(any(Orders.class), eq(BookingResponse.class))).thenReturn(mockBookingResponse);

        //Act
        BookingResponse response = bookingService.createBooking(validRequest);

        //Assert
        assertNotNull(response);
        assertEquals(validService1.getPrice(), response.getTotalCost());
        assertEquals(validService1.getServiceName(), response.getServiceType());
        assertTrue(response.getMessage().startsWith("Booking created successfully with 1 service(s)"));

        // Verify serviceRepository.findById
        verify(serviceRepository, times(1)).findById(anyLong());
        verify(serviceRepository, times(1)).findById(VALID_SERVICE_ID_1);
    }
}