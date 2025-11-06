package BE.service;

import BE.entity.Component;
import BE.model.response.AIPartResponse;
import BE.repository.ComponentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIPartService {

    private final ComponentRepository componentRepository;

    // Improved AI logic using real database data
    public AIPartResponse calculateSuggestedMin(String code) {
        Component component = componentRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phụ tùng có mã: " + code));

        int currentQuantity = component.getQuantity();
        int currentMin = component.getMinQuantity();

        // Step 1: Estimate daily usage (simple heuristic)
        // Assume the "minQuantity" was based on ~30 days of expected usage
        double avgDaily = Math.max(0.5, currentMin / 30.0); // ensure not zero

        // Step 2: Safety stock & buffer levels
        int safetyStock = (int) Math.ceil(avgDaily * 5);  // ~5 days of safety
        int buffer = (int) Math.ceil(avgDaily * 2);       // small buffer

        // Step 3: Suggest adjustment based on current quantity vs min
        int suggestedMin;

        if (currentQuantity <= 0) {
            // Out of stock → keep or slightly raise min
            suggestedMin = currentMin + safetyStock;
        } else if (currentQuantity < currentMin) {
            // Running low → raise min moderately
            suggestedMin = currentMin + (int) Math.ceil((currentMin - currentQuantity) * 0.5);
        } else if (currentQuantity > currentMin * 1.5) {
            // Often excess → reduce min a bit
            suggestedMin = (int) Math.ceil(currentMin * 0.9);
        } else {
            // Normal → keep similar
            suggestedMin = currentMin;
        }

        // Ensure suggestedMin is at least above safety threshold
        suggestedMin = Math.max(suggestedMin, safetyStock + buffer);

        // Step 4: Prepare response object
        AIPartResponse response = new AIPartResponse();
        response.setCode(code);
        response.setCurrentMin(currentMin);
        response.setAvgDaily(avgDaily);
        response.setLookbackDays(30);
        response.setForecast((int) Math.ceil(avgDaily * 7)); // 7-day forecast
        response.setSafetyStock(safetyStock);
        response.setBuffer(buffer);
        response.setSuggestedMin(suggestedMin);

        return response;
    }

    // Apply suggestion (update minQuantity)
    public void updateMinQuantityByCode(String code, int newMin) {
        Component component = componentRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phụ tùng có mã: " + code));

        component.setMinQuantity(newMin);
        componentRepository.save(component);
    }
}
