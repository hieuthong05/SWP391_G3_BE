package BE.controller;

import BE.model.DTO.ShiftDTO;
import BE.service.ShiftService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shifts") // Endpoint cơ sở cho Shift
@SecurityRequirement(name="api")
@RequiredArgsConstructor
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @GetMapping("/getAll")
    public ResponseEntity<List<ShiftDTO>> getAllShifts() {
        List<ShiftDTO> shifts = shiftService.getAllShifts();
        return ResponseEntity.ok(shifts);
    }
}