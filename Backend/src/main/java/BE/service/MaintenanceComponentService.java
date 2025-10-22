package BE.service;

import BE.entity.Component;
import BE.entity.Maintenance;
import BE.entity.MaintenanceComponent;
import BE.model.DTO.MaintenanceComponentDTO;
import BE.model.response.MaintenanceComponentResponse;
import BE.repository.ComponentRepository;
import BE.repository.MaintenanceComponentRepository;
import BE.repository.MaintenanceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MaintenanceComponentService {

    @Autowired
    private MaintenanceComponentRepository maintenanceComponentRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public MaintenanceComponentResponse addComponentToMaintenance(Long maintenanceId, MaintenanceComponentDTO dto) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found with ID: " + maintenanceId));

        Component component = componentRepository.findById(dto.getComponentId())
                .orElseThrow(() -> new EntityNotFoundException("Component not found with ID: " + dto.getComponentId()));

        Optional<MaintenanceComponent> existingOpt = maintenanceComponentRepository
                .findByMaintenance_MaintenanceIDAndComponent_ComponentID(maintenanceId, dto.getComponentId());

        MaintenanceComponent mc;
        if (existingOpt.isPresent()) {
            mc = existingOpt.get();
            // Quyết định logic: Cộng dồn hay thay thế? Ở đây đang cộng dồn.
            mc.setQuantity(mc.getQuantity() + dto.getQuantity());
        } else {
            mc = new MaintenanceComponent();
            mc.setMaintenance(maintenance);
            mc.setComponent(component);
            mc.setQuantity(dto.getQuantity());
        }

        MaintenanceComponent savedMc = maintenanceComponentRepository.save(mc);
        return convertToResponse(savedMc);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceComponentResponse> getComponentsByMaintenanceId(Long maintenanceId) {
        if (!maintenanceRepository.existsById(maintenanceId)) {
            throw new EntityNotFoundException("Maintenance not found with ID: " + maintenanceId);
        }
        List<MaintenanceComponent> components = maintenanceComponentRepository.findByMaintenance_MaintenanceID(maintenanceId);
        return components.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaintenanceComponentResponse updateComponentQuantity(Long maintenanceComponentId, int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        MaintenanceComponent mc = maintenanceComponentRepository.findById(maintenanceComponentId)
                .orElseThrow(() -> new EntityNotFoundException("MaintenanceComponent not found with ID: " + maintenanceComponentId));

        // Kiểm tra xem báo giá đã được duyệt chưa trước khi cho sửa đổi
        if (mc.getMaintenance() != null && mc.getMaintenance().getInvoice() != null &&
                "APPROVED".equalsIgnoreCase(mc.getMaintenance().getInvoice().getStatus())) { // Giả sử Invoice có status
            throw new IllegalStateException("Cannot update component quantity after the quotation is approved.");
        }


        mc.setQuantity(newQuantity);
        MaintenanceComponent updatedMc = maintenanceComponentRepository.save(mc);
        return convertToResponse(updatedMc);
    }

    @Transactional
    public void removeComponentFromMaintenance(Long maintenanceComponentId) {
        MaintenanceComponent mc = maintenanceComponentRepository.findById(maintenanceComponentId)
                .orElseThrow(() -> new EntityNotFoundException("MaintenanceComponent not found with ID: " + maintenanceComponentId));

        // Kiểm tra xem báo giá đã được duyệt chưa trước khi cho xóa
        if (mc.getMaintenance() != null && mc.getMaintenance().getInvoice() != null &&
                "APPROVED".equalsIgnoreCase(mc.getMaintenance().getInvoice().getStatus())) { // Giả sử Invoice có status
            throw new IllegalStateException("Cannot remove component after the quotation is approved.");
        }

        maintenanceComponentRepository.delete(mc);
    }

    private MaintenanceComponentResponse convertToResponse(MaintenanceComponent mc) {
        MaintenanceComponentResponse response = modelMapper.map(mc, MaintenanceComponentResponse.class);
        if (mc.getMaintenance() != null) {
            response.setMaintenanceId(mc.getMaintenance().getMaintenanceID());
        }
        if (mc.getComponent() != null) {
            response.setComponentId(mc.getComponent().getComponentID());
            response.setComponentName(mc.getComponent().getName());
            response.setComponentCode(mc.getComponent().getCode());
            response.setComponentPrice(mc.getComponent().getPrice());
        }
        return response;
    }
}