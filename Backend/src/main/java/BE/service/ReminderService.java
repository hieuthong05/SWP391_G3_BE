package BE.service;

import BE.entity.Orders;
import BE.entity.ServicePackage;
import BE.entity.Vehicle;
import BE.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReminderService {

    @Autowired
    private VehicleRepository vehicleRepository;


    public List<String> checkReminders(Vehicle vehicle)
    {
        List<String> reminders = new ArrayList<>();

        if (vehicle.getOrders() != null)
        {
            for (Orders order : vehicle.getOrders())
            {
                if (order.getServicePackages() != null)
                {
                    for(ServicePackage sp : order.getServicePackages())
                    {
                        if (sp.getIntervalKm() != null)
                        {
                            if (vehicle.getMileage() >= sp.getIntervalKm())
                            {
                                reminders.add("Xe: " + vehicle.getLicensePlate() +
                                        " cần bảo dưỡng (đã đạt " + vehicle.getMileage() + " km)");
                            }
                        }

                        if (sp.getIntervalMonths() != null)
                        {
                            LocalDate nextMaintenance = vehicle.getDayCreated().toLocalDate().plusMonths(sp.getIntervalMonths());
                            if (LocalDate.now().isAfter(nextMaintenance))
                            {
                                reminders.add("Xe: " + vehicle.getLicensePlate() +
                                        " cần bảo dưỡng định kỳ (đã tới hạn " + sp.getIntervalMonths() + " tháng)");
                            }
                        }
                    }
                }
            }
        }

        return reminders;
    }

    public Map<String, List<String>> getRemindersForCustomer(Long customerId)
    {
        List<Vehicle> vehicles = vehicleRepository.findByCustomerCustomerID(customerId);
        Map<String, List<String>> result = new HashMap<>();

        for (Vehicle vehicle : vehicles)
        {
            List<String> reminders = checkReminders(vehicle);
            result.put(vehicle.getLicensePlate(), reminders);
        }
        return result;
    }
}
