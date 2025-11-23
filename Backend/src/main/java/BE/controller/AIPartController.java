package BE.controller;

import BE.model.response.AIPartResponse;
import BE.service.AIPartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/parts")
@RequiredArgsConstructor
public class AIPartController {

    private final AIPartService aiPartService;

    // AI Suggestion Endpoint
    @GetMapping("/suggested-min/{code}")
    public AIPartResponse getSuggestedMin(
            @PathVariable String code,
            @RequestParam(defaultValue = "7") int days) { // Default is 7
        return aiPartService.calculateSuggestedMin(code, days);
    }


    // Apply AI Suggestion Endpoint
    @PostMapping("/apply-suggested-min/{code}")
    public ResponseEntity<String> applySuggestedMin(
            @PathVariable String code,
            @RequestBody Map<String, Integer> payload) {

        int suggestedMin = payload.get("suggestedMin");
        aiPartService.updateMinQuantityByCode(code, suggestedMin);
        return ResponseEntity.ok("Đã cập nhật số lượng tối thiểu theo gợi ý AI.");
    }
}
