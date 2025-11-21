package BE.controller;

import BE.model.DTO.ServiceDTO;
import BE.model.response.ServiceResponse;
import BE.service.ServicesService;
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

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class ServiceController {

    @Autowired
    private ServicesService servicesService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getServiceById/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        try {
            ServiceResponse response = servicesService.getServiceById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getAll")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = servicesService.getAllService();
        return ResponseEntity.ok(services);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/searchByType/{serviceType}")
    public ResponseEntity<List<ServiceResponse>> getServicesByType(@PathVariable String serviceType) {
        List<ServiceResponse> services = servicesService.getServiceByType(serviceType);
        return ResponseEntity.ok(services);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<List<ServiceResponse>> getServicesByName(@RequestParam String name) {
        List<ServiceResponse> services = servicesService.getServiceByName(name);
        return ResponseEntity.ok(services);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/create")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceResponse newService = servicesService.createService(serviceDTO);
        return new ResponseEntity<>(newService, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ServiceResponse> updateService(@PathVariable Long id,@Valid @RequestBody ServiceDTO serviceDTO) {
        try {
            ServiceResponse updatedService = servicesService.updateService(id, serviceDTO);
            return ResponseEntity.ok(updatedService);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        try {
            servicesService.deleteService(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
