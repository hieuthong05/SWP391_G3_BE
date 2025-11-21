package BE.controller;

import BE.model.request.CertificationRequest;
import BE.model.response.CertificationResponse;
import BE.service.CertificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ev-certifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "EV Cert API")
public class CertificationController {

    @Autowired
    private CertificationService certificationService;

    @PostMapping("/create")
    public ResponseEntity<CertificationResponse> createCertification(@RequestBody CertificationRequest request) {
        try {
            CertificationResponse created = certificationService.createCertification(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<CertificationResponse>> getAllCertifications() {
        List<CertificationResponse> certifications = certificationService.getAllCertifications();
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificationResponse> getCertificationById(@PathVariable Long id) {
        try {
            CertificationResponse certification = certificationService.getCertificationById(id);
            return ResponseEntity.ok(certification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CertificationResponse> updateCertification(
            @PathVariable Long id,
            @RequestBody CertificationRequest request) {
        try {
            CertificationResponse updated = certificationService.updateCertification(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        try {
            certificationService.deleteCertification(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/employee/{employeeID}")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByEmployee(@PathVariable Long employeeID) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByEmployee(employeeID);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByLevel(@PathVariable String level) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByLevel(level);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/issued-by/{issuedBy}")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByIssuedBy(@PathVariable String issuedBy) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByIssuedBy(issuedBy);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/issued-date")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByIssuedDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByIssuedDate(date);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/issued-date-range")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByIssuedDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByIssuedDateRange(startDate, endDate);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/expiration-date")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByExpirationDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByExpirationDate(date);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/expiration-date-range")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByExpirationDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByExpirationDateRange(startDate, endDate);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByStatus(@PathVariable boolean status) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByStatus(status);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/active/{active}")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByActive(@PathVariable boolean active) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByActive(active);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/employee/{employeeID}/active/{active}")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByEmployeeAndActive(
            @PathVariable Long employeeID,
            @PathVariable boolean active) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByEmployeeAndActive(employeeID, active);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/employee/{employeeID}/level/{level}")
    public ResponseEntity<List<CertificationResponse>> getCertificationsByEmployeeAndLevel(
            @PathVariable Long employeeID,
            @PathVariable String level) {
        List<CertificationResponse> certifications = certificationService.getCertificationsByEmployeeAndLevel(employeeID, level);
        return ResponseEntity.ok(certifications);
    }
}
