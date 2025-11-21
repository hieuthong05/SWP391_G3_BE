package BE.controller;

import BE.model.DTO.AllMonthsPerformanceReportDTO;
import BE.model.DTO.DashboardStatisticsDTO;
import BE.model.DTO.MonthlyPerformanceReportDTO;
import BE.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@CrossOrigin(origins = "*")
@Tag(name = "Statistics API")
@PreAuthorize("hasAnyAuthority('staff', 'admin')")
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

    //==============================PERFORMANCE====================================

//     * API lấy báo cáo hiệu năng của tất cả technician theo TẤT CẢ các tháng/năm
//     * GET /api/technician-performance/all-months

    @GetMapping("/technician-performance/all-months")
    public ResponseEntity<AllMonthsPerformanceReportDTO> getAllMonthsPerformanceReport()
    {
        try
        {
            AllMonthsPerformanceReportDTO report = statisticService.getAllMonthsPerformanceReport();
            return ResponseEntity.ok(report);
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

//     * API lấy báo cáo hiệu năng của tất cả technician theo tháng/năm cụ thể
//     * GET /api/technician-performance/monthly?month=11&year=2024

    @GetMapping("/technician-performance/monthly")
    public ResponseEntity<MonthlyPerformanceReportDTO> getMonthlyPerformanceReport(
            @RequestParam Integer month,
            @RequestParam Integer year)
    {
        try
        {
            MonthlyPerformanceReportDTO report = statisticService.getMonthlyPerformanceReport(month, year);
            return ResponseEntity.ok(report);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body(null);
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

//     * API lấy báo cáo hiệu năng của tất cả technician theo tháng hiện tại
//     * GET /api/technician-performance/monthly/current

    @GetMapping("/technician-performance/monthly/current")
    public ResponseEntity<MonthlyPerformanceReportDTO> getCurrentMonthPerformanceReport()
    {
        LocalDate now = LocalDate.now();
        MonthlyPerformanceReportDTO report = statisticService.getMonthlyPerformanceReport(
                now.getMonthValue(),
                now.getYear()
        );
        return ResponseEntity.ok(report);
    }
}
