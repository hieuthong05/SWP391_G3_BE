package BE.service;

import BE.model.DTO.DashboardStatisticsDTO;
import BE.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private MaintenanceChecklistRepository maintenanceChecklistRepository;


    public List<Map<String, Object>> getMonthlyStatistics()
    {
        List<Map<String, Object>> rawData = invoiceRepository.getMonthlyRevenue();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> row : rawData)
        {
            int month = ((Number) row.get("month")).intValue();
            int year = ((Number) row.get("year")).intValue();
            double revenue = ((Number) row.get("totalRevenue")).doubleValue();

            // Giả sử chi phí = 70% doanh thu (bạn có thể thay bằng công thức thật nếu có)
            double expense = revenue * 0.7;
            double profit = revenue - expense;

            Map<String, Object> data = new HashMap<>();
            data.put("month", month + "/" + year);
            data.put("revenue", revenue);
            data.put("expense", expense);
            data.put("profit", profit);

            result.add(data);
        }

        return result;
    }

    public DashboardStatisticsDTO getDashboardStatistics()
    {
        long customers = userRepository.countActiveCustomers();
        long staff = userRepository.countActiveStaff();
        long technicians = userRepository.countActiveTechnicians();
        long totalActiveUsers = userRepository.countAllActiveUsers();
        long activeVehicles = vehicleRepository.countActiveVehicles();
        long totalOrders = ordersRepository.count();
        long totalMaintenances = maintenanceRepository.count();

        return new DashboardStatisticsDTO(
                customers,
                staff,
                technicians,
                totalActiveUsers,
                activeVehicles,
                totalOrders,
                totalMaintenances
        );
    }

    public Map<String, Object> getMonthlyStatistics(int year)
    {
        Map<String, Object> result = new LinkedHashMap<>();

        // --- 1. Thống kê order ---
        List<Object[]> orderData = ordersRepository.countOrdersByMonth(year);
        Map<Integer, Long> orderCountByMonth = new LinkedHashMap<>();
        for (Object[] row : orderData)
        {
            orderCountByMonth.put((Integer) row[0], (Long) row[1]);
        }

        // --- 2. Thống kê maintenance ---
        List<Object[]> maintenanceData = maintenanceRepository.countMaintenanceByMonth(year);
        Map<Integer, Long> maintenanceCountByMonth = new LinkedHashMap<>();
        for (Object[] row : maintenanceData)
        {
            maintenanceCountByMonth.put((Integer) row[0], (Long) row[1]);
        }

        // --- 3. Tìm tháng có nhiều order nhất ---
        Optional<Map.Entry<Integer, Long>> maxOrderMonth = orderCountByMonth.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        Integer monthWithMostOrders = maxOrderMonth.map(Map.Entry::getKey).orElse(null);
        Long maxOrderCount = maxOrderMonth.map(Map.Entry::getValue).orElse(0L);

        result.put("year", year);
        result.put("ordersByMonth", orderCountByMonth);
        result.put("maintenanceByMonth", maintenanceCountByMonth);
        result.put("monthWithMostOrders", monthWithMostOrders);
        result.put("maxOrderCount", maxOrderCount);

        return result;
    }

    public Map<String, Object> getUserStatisticsByMonth()
    {
        List<Object[]> results = userRepository.countUsersByRoleAndMonth();

        // Map<String, Map<String, Long>> => "1/2025" -> {"CUSTOMER": 10, "STAFF": 3, "TECHNICIAN": 2}
        Map<String, Map<String, Long>> groupedData = new LinkedHashMap<>();

        for (Object[] row : results)
        {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            String role = ((String) row[2]).toUpperCase();
            long count = ((Number) row[3]).longValue();

            String monthYear = month + "/" + year;

            groupedData.putIfAbsent(monthYear, new HashMap<>());
            Map<String, Long> roleCounts = groupedData.get(monthYear);
            roleCounts.put(role, count);
        }

        // Tìm tháng có số lượng lớn nhất cho từng role
        Map<String, String> mostActiveMonths = new HashMap<>();
        Map<String, Long> maxCounts = new HashMap<>();

        for (var entry : groupedData.entrySet())
        {
            String monthYear = entry.getKey();
            Map<String, Long> roles = entry.getValue();

            for (var roleEntry : roles.entrySet())
            {
                String role = roleEntry.getKey();
                long count = roleEntry.getValue();

                if (!maxCounts.containsKey(role) || count > maxCounts.get(role))
                {
                    maxCounts.put(role, count);
                    mostActiveMonths.put(role, monthYear);
                }
            }
        }

        // Chuẩn bị kết quả trả về
        List<Map<String, Object>> statistics = new ArrayList<>();

        for (var entry : groupedData.entrySet())
        {
            String monthYear = entry.getKey();
            Map<String, Object> data = new HashMap<>();
            data.put("month", monthYear);
            data.put("CUSTOMER", entry.getValue().getOrDefault("CUSTOMER", 0L));
            data.put("STAFF", entry.getValue().getOrDefault("STAFF", 0L));
            data.put("TECHNICIAN", entry.getValue().getOrDefault("TECHNICIAN", 0L));
            statistics.add(data);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("statistics", statistics);
        response.put("mostActiveMonths", mostActiveMonths);

        return response;
    }

    public Map<String, Object> getChecklistFailureStatistics()
    {
        List<Object[]> results = maintenanceChecklistRepository.countFailedChecklists();

        List<Map<String, Object>> statistics = new ArrayList<>();

        for (Object[] row : results)
        {
            Map<String, Object> data = new HashMap<>();
            data.put("checklist_id", row[0]);
            data.put("checklist_name", row[1]);
            data.put("fail_count", row[2]);
            statistics.add(data);
        }

        // Lấy 2 checklist fail nhiều nhất
        List<Map<String, Object>> top2Fails = statistics.stream()
                .sorted((a, b) -> Long.compare((Long) b.get("fail_count"), (Long) a.get("fail_count")))
                .limit(2)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("failure_statistics", statistics);
        response.put("top2_failure_trends", top2Fails);

        return response;
    }
}
