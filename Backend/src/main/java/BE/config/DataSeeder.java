//package BE.config;
//
//import BE.entity.*;
//import BE.repository.*;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//
//@Configuration
//public class DataSeeder {
//
//    @Bean
//    CommandLineRunner initData(
//            ServiceCenterRepository serviceCenterRepo,
//            ShiftRepository shiftRepo,
//            AdminRepository adminRepo,
//            EmployeeRepository employeeRepo,
//            CustomerRepository customerRepo,
//            UserRepository userRepo,
//            ServiceRepository serviceRepo
//    ) {
//        return args -> {
//
//            // Không chạy lại nếu đã có dữ liệu
//            if (adminRepo.count() > 0 || customerRepo.count() > 0) {
//                System.out.println("⚠️ Database already has data, skipping seeding.");
//                return;
//            }
//
//            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//            // === 1️⃣ Tạo Service Center ===
//            ServiceCenter center = new ServiceCenter();
//            center.setName("Wise Auto Center");
//            center.setAddress("123 Main Street");
//            center.setLocation("Ho Chi Minh City");
//            center.setPhone("0901234567");
//            center.setEmail("center@wiseauto.com");
//            center.setOpenTime("08:00");
//            center.setCloseTime("18:00");
//            center.setStatus("ACTIVE");
//            serviceCenterRepo.save(center);
//
//            // === 2️⃣ Tạo Shift ===
//            Shift shift = new Shift();
//            shift.setName("Morning Shift");
//            shift.setServiceCenter(center);
//            shift.setShift_date(LocalDate.now());
//            shift.setStart_time(LocalTime.of(8, 0));
//            shift.setEnd_time(LocalTime.of(16, 0));
//            shift.setStatus(true);
//            shiftRepo.save(shift);
//
//            // === 3️⃣ Tạo Admin ===
//            Admin admin = new Admin();
//            admin.setName("System Admin");
//            admin.setEmail("admin@wiseauto.com");
//            admin.setPhone("0900000001");
//            admin.setPassword(encoder.encode("admin123"));
//            admin.setRole("SUPER_ADMIN");
//            admin.setServiceCenter(center);
//            admin.setGender("Male");
//            admin.setSalary(15000000.0);
//            admin.setAddress("District 1, HCM");
//            admin.setBirth(LocalDate.of(1990, 1, 1));
//            admin.setShift(shift);
//            admin.setStatus("ACTIVE");
//            adminRepo.save(admin);
//
//            // === 4️⃣ Tạo Employee ===
//            Employee emp = new Employee();
//            emp.setName("John Technician");
//            emp.setEmail("john@wiseauto.com");
//            emp.setPhone("0900000002");
//            emp.setPassword(encoder.encode("tech123"));
//            emp.setGender("Male");
//            emp.setRole("TECHNICIAN");
//            emp.setServiceCenter(center);
//            emp.setShift(shift);
//            emp.setStatus(true);
//            emp.setSalary(10000000.0);
//            emp.setAddress("District 3, HCM");
//            emp.setBirth(LocalDate.of(1995, 5, 10));
//            employeeRepo.save(emp);
//
//            // === 5️⃣ Tạo Customer ===
//            Customer customer = new Customer();
//            customer.setName("Alice Nguyen");
//            customer.setEmail("alice@example.com");
//            customer.setPhone("0900000003");
//            customer.setPassword(encoder.encode("alice123"));
//            customer.setGender("Female");
//            customer.setAddress("District 5, HCM");
//            customer.setBirth(LocalDate.of(1998, 3, 15));
//            customer.setStatus(true);
//            customerRepo.save(customer);
//
//            // === 6️⃣ Tạo User Login tương ứng ===
//            User userAdmin = new User();
//            userAdmin.setPhone(admin.getPhone());
//            userAdmin.setPassword(admin.getPassword());
//            userAdmin.setRole("SUPER_ADMIN");
//            userAdmin.setRefId(admin.getAdminID());
//            userAdmin.setRefType("ADMIN");
//            userAdmin.setStatus(true);
//
//            User userEmp = new User();
//            userEmp.setPhone(emp.getPhone());
//            userEmp.setPassword(emp.getPassword());
//            userEmp.setRole("TECHNICIAN");
//            userEmp.setRefId(emp.getEmployeeID());
//            userEmp.setRefType("EMPLOYEE");
//            userEmp.setStatus(true);
//
//            User userCus = new User();
//            userCus.setPhone(customer.getPhone());
//            userCus.setPassword(customer.getPassword());
//            userCus.setRole("CUSTOMER");
//            userCus.setRefId(customer.getCustomerID());
//            userCus.setRefType("CUSTOMER");
//            userCus.setStatus(true);
//
//            userRepo.saveAll(List.of(userAdmin, userEmp, userCus));
//
//            // === 7️⃣ Tạo vài dịch vụ mẫu ===
//            Service oilChange = new Service();
//            oilChange.setServiceName("Oil Change");
//            oilChange.setDescription("Thay nhớt động cơ");
//            oilChange.setServiceType("Maintenance");
//            oilChange.setEstimatedTime("1 hour");
//            oilChange.setPrice(500000.0);
//            oilChange.setWarrantyPeriod(3);
//            oilChange.setServiceStatus("ACTIVE");
//            serviceRepo.save(oilChange);
//
//            Service tireCheck = new Service();
//            tireCheck.setServiceName("Tire Check");
//            tireCheck.setDescription("Kiểm tra và bơm lốp");
//            tireCheck.setServiceType("Inspection");
//            tireCheck.setEstimatedTime("30 minutes");
//            tireCheck.setPrice(200000.0);
//            tireCheck.setWarrantyPeriod(1);
//            tireCheck.setServiceStatus("ACTIVE");
//            serviceRepo.save(tireCheck);
//
//            System.out.println("✅ Database seeded successfully with sample data!");
//        };
//    }
//}
