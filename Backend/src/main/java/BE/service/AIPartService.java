package BE.service;

import BE.entity.Component;
import BE.model.response.AIPartResponse;
import BE.repository.ComponentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AIPartService {

    private final ComponentRepository componentRepository;

    // ✅ Dummy AI logic for now — can replace with actual predictive model later
    public AIPartResponse calculateSuggestedMin(String code) {
        Component component = componentRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phụ tùng có mã: " + code));

        // --- Simple AI Simulation ---
        Random rand = new Random();
        double avgDaily = 5 + rand.nextDouble() * 10; // giả định mức tiêu thụ/ngày
        int lookbackDays = 30;
        int forecast = (int) Math.ceil(avgDaily * 7);
        int safetyStock = (int) Math.ceil(avgDaily * 3);
        int buffer = (int) Math.ceil(avgDaily);
        int suggestedMin = (int) Math.ceil(avgDaily * 5 + buffer);

        // --- Prepare response ---
        AIPartResponse response = new AIPartResponse();
        response.setCode(code);
        response.setCurrentMin(component.getMinQuantity());
        response.setAvgDaily(avgDaily);
        response.setLookbackDays(lookbackDays);
        response.setForecast(forecast);
        response.setSafetyStock(safetyStock);
        response.setBuffer(buffer);
        response.setSuggestedMin(suggestedMin);

        return response;
    }

    // ✅ Apply suggestion (update minQuantity)
    public void updateMinQuantityByCode(String code, int newMin) {
        Component component = componentRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phụ tùng có mã: " + code));

        component.setMinQuantity(newMin);
        componentRepository.save(component);
    }
}
