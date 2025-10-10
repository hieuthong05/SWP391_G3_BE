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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    @GetMapping("/getAll")
    public ResponseEntity<List<ComponentResponse>> getAllComponents() {
        List<ComponentResponse> components = componentService.getAllComponent();
        return ResponseEntity.ok(components);
    }

    @GetMapping("/getComponentsByID/{id}")
    public ResponseEntity<ComponentResponse> getComponentById(@PathVariable Long id) {
        try {
            ComponentResponse response = componentService.getComponentById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

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

    @GetMapping("/search/by-type")
    public ResponseEntity<List<ComponentResponse>> getComponentByType(@RequestParam String type) {
        List<ComponentResponse> components = componentService.getComponentByType(type);
        if (components.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(components);
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<List<ComponentResponse>> getComponentByName(@RequestParam String name) {
        List<ComponentResponse> components = componentService.getComponentByName(name);
        if (components.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(components);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ComponentResponse>> getLowStockComponent() {
        List<ComponentResponse> components = componentService.getLowStockComponent();
        if (components.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(components);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createComponent(@Valid @RequestBody ComponentDTO componentDTO) {
        try {
            ComponentResponse response = componentService.createComponent(componentDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("Service Center not found for the given ID.");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateComponent(
            @PathVariable Long id,
            @Valid @RequestBody ComponentDTO componentDTO) {
        try {
            ComponentResponse response = componentService.updateComponent(id, componentDTO);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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