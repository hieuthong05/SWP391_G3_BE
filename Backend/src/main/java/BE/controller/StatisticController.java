package BE.controller;

import BE.service.StatisticService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@SecurityRequirement(name = "api")
@CrossOrigin(origins = "*")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyStatistics()
    {
        List<Map<String, Object>> stats = statisticService.getMonthlyStatistics();
        return ResponseEntity.ok(stats);
    }
}
