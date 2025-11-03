package BE.controller;

import BE.model.DTO.DashboardStatisticsDTO;
import BE.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@CrossOrigin(origins = "*")
@Tag(name = "Statistics API")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyStatistics()
    {
        List<Map<String, Object>> stats = statisticService.getMonthlyStatistics();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get system dashboard statistics")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatisticsDTO> getDashboardStatistics()
    {
        DashboardStatisticsDTO statistics = statisticService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    // API: /api/statistics/monthly?year=2025
    @GetMapping("/orders/monthly")
    public Map<String, Object> getMonthlyStatistics(@RequestParam int year)
    {
        return statisticService.getMonthlyStatistics(year);
    }

    @GetMapping("/accounts")
    @Operation(summary = "Thống kê số lượng account theo role và tháng/năm")
    public Map<String, Object> getUserStatisticsByMonth()
    {
        return statisticService.getUserStatisticsByMonth();
    }

    @Operation(summary = "Thống kê số lượng checklist fail và xu hướng hỏng hóc")
    @GetMapping("/failures")
    public Map<String, Object> getChecklistFailureStats()
    {
        return statisticService.getChecklistFailureStatistics();
    }
}
