package BE.controller;

import BE.model.DTO.ComponentDTO;
import BE.model.response.ComponentResponse;
import BE.service.ComponentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/components")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @GetMapping("/getAll")
    public ResponseEntity<List<ComponentResponse>> getAllComponents() {
        List<ComponentResponse> components = componentService.getAllComponent();
        return ResponseEntity.ok(components);
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @GetMapping("/getComponentsByID/{id}")
    public ResponseEntity<ComponentResponse> getComponentById(@PathVariable Long id) {
        try {
            ComponentResponse response = componentService.getComponentById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @GetMapping("/search/by-service-center/{serviceCenterID}")
    public ResponseEntity<List<ComponentResponse>> getComponentByServiceCenter(@PathVariable Long serviceCenterID) {
        try {
            List<ComponentResponse> components = componentService.getComponentByServiceCenter(serviceCenterID);
            if (components.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(components);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @GetMapping("/search/by-type")
    public ResponseEntity<List<ComponentResponse>> getComponentByType(@RequestParam String type) {
        List<ComponentResponse> components = componentService.getComponentByType(type);
        if (components.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(components);
    }

    @PreAuthorize("hasAnyAuthority('technician', 'staff', 'admin')")
    @GetMapping("/search/by-name")
    public ResponseEntity<List<ComponentResponse>> getComponentByName(@RequestParam String name) {
        List<ComponentResponse> components = componentService.getComponentByName(name);
        if (components.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(components);
    }

    @PreAuthorize("hasAnyAuthority('staff', 'admin','technician')")
    @GetMapping("/low-stock")
    public ResponseEntity<List<ComponentResponse>> getLowStockComponent() {
        List<ComponentResponse> components = componentService.getLowStockComponent();
        if (components.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(components);
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createComponent(@Valid @ModelAttribute ComponentDTO componentDTO) throws Exception
    {
        try {
            ComponentResponse response = componentService.createComponent(componentDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("Service Center not found for the given ID.");
        }
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 1. Thêm dòng này để nhận file
    public ResponseEntity<?> updateComponent(
            @PathVariable Long id,
            @Valid @ModelAttribute ComponentDTO componentDTO // 2. Đổi @RequestBody thành @ModelAttribute
    ) throws IOException {
        try {
            ComponentResponse response = componentService.updateComponent(id, componentDTO);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        try {
            componentService.deleteComponent(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}