package BE.controller;

import BE.model.DTO.ServiceCenterDTO;
import BE.model.response.ServiceCenterResponse;
import BE.service.ServiceCenterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "api")
@RestController
@RequestMapping("/api/service-centers")
@RequiredArgsConstructor
public class ServiceCenterController {

    @Autowired
    private ServiceCenterService serviceCenterService;

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @GetMapping("/getAll")
    public ResponseEntity<List<ServiceCenterResponse>> getAllServiceCenters() {
        List<ServiceCenterResponse> serviceCenters = serviceCenterService.getAllServiceCenter();
        return ResponseEntity.ok(serviceCenters);
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @GetMapping("/getBy/{id}")
    public ResponseEntity<ServiceCenterResponse> getServiceCenterById(@PathVariable Long id) {
        try {
            ServiceCenterResponse response = serviceCenterService.getServiceCenterById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @GetMapping("/search/by-name")
    public ResponseEntity<List<ServiceCenterResponse>> getServiceCenterByName(@RequestParam String name) {
        List<ServiceCenterResponse> serviceCenters = serviceCenterService.getServiceCenterByName(name);
        if (serviceCenters.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceCenters);
    }

    @PreAuthorize("hasAnyAuthority('customer', 'staff', 'admin', 'technician')")
    @GetMapping("/search/by-location")
    public ResponseEntity<List<ServiceCenterResponse>> getServiceCenterByLocation(@RequestParam String location) {
        List<ServiceCenterResponse> serviceCenters = serviceCenterService.getServiceCenterByLocation(location);
        if (serviceCenters.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceCenters);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/create")
    public ResponseEntity<?> createServiceCenter(@Valid @RequestBody ServiceCenterDTO serviceCenterDTO) {
        try {
            ServiceCenterResponse response = serviceCenterService.createServiceCenter(serviceCenterDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateServiceCenter(
            @PathVariable Long id,
            @Valid @RequestBody ServiceCenterDTO serviceCenterDTO) {
        try {
            ServiceCenterResponse response = serviceCenterService.updateServiceCenter(id, serviceCenterDTO);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteServiceCenter(@PathVariable Long id) {
        try {
            serviceCenterService.deleteServiceCenter(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
