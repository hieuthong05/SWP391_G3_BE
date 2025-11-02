package BE.service;

import BE.model.DTO.DashboardStatisticsDTO;
import BE.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
