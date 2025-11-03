package BE.service;

import BE.entity.ServiceCenter;
import BE.model.DTO.ComponentDTO;
import BE.model.response.ComponentResponse;
import BE.repository.ComponentRepository;
import BE.repository.ServiceCenterRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class ComponentService {

    @Autowired
    ComponentRepository componentRepository;

    @Autowired
    ServiceCenterRepository serviceCenterRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public ComponentResponse getComponentById(Long id) {
        BE.entity.Component component = componentRepository.findByComponentIDAndStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Component not found with id: " + id));

        ComponentResponse componentResponse = new ComponentResponse();
        modelMapper.map(component, componentResponse);

        if (component.getServiceCenter() != null) {
            componentResponse.setServiceCenterID(component.getServiceCenter().getServiceCenterID());
            componentResponse.setServiceCenterName(component.getServiceCenter().getName());
        }

        return componentResponse;
    }

    @Transactional
    public ComponentResponse createComponent(ComponentDTO componentDTO) throws Exception {
        if (componentRepository.findByCode(componentDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Component code already exists");
        }

        BE.entity.Component component = new BE.entity.Component();

        component.setComponentID(null);
        component.setName(componentDTO.getName());
        component.setCode(componentDTO.getCode());
        component.setType(componentDTO.getType());
        component.setDescription(componentDTO.getDescription());
        component.setPrice(componentDTO.getPrice());
        component.setQuantity(componentDTO.getQuantity());
        component.setMinQuantity(componentDTO.getMinQuantity());
        component.setSupplierName(componentDTO.getSupplierName());
        component.setStatus("active");

        if (componentDTO.getServiceCenterID() != null) {
            ServiceCenter serviceCenter = serviceCenterRepository.findById(componentDTO.getServiceCenterID())
                    .orElseThrow(() -> new EntityNotFoundException("Service Center not found"));
            component.setServiceCenter(serviceCenter);
        }

        // Upload image if available
        String imageUrl = null;
        if (componentDTO.getImage() != null && !componentDTO.getImage().isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(componentDTO.getImage(), "components");
        }
        component.setImageUrl(imageUrl);

        BE.entity.Component savedComponent = componentRepository.save(component);
        ComponentResponse componentResponse = new ComponentResponse();
        modelMapper.map(savedComponent, componentResponse);

        if (savedComponent.getServiceCenter() != null) {
            componentResponse.setServiceCenterID(savedComponent.getServiceCenter().getServiceCenterID());
            componentResponse.setServiceCenterName(savedComponent.getServiceCenter().getName());
        }

        return componentResponse;
    }

    // ✅ FIXED UPDATE METHOD (no more foreign key errors)
    @Transactional
    public ComponentResponse updateComponent(Long id, ComponentDTO componentDTO) throws IOException {
        BE.entity.Component component = componentRepository.findByComponentIDAndStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Component not found with id: " + id));

        if (componentRepository.findByCodeAndComponentIDNot(componentDTO.getCode(), id).isPresent()) {
            throw new IllegalArgumentException("Component code already exists");
        }

        // ✅ Only update editable fields, ignore checklist_id & status
        component.setPrice(componentDTO.getPrice());
        component.setQuantity(componentDTO.getQuantity());
        component.setMinQuantity(componentDTO.getMinQuantity());
        component.setSupplierName(componentDTO.getSupplierName());
        component.setDescription(componentDTO.getDescription());

        // ✅ Update Service Center if provided
        if (componentDTO.getServiceCenterID() != null) {
            ServiceCenter serviceCenter = serviceCenterRepository.findById(componentDTO.getServiceCenterID())
                    .orElseThrow(() -> new EntityNotFoundException("Service Center not found"));
            component.setServiceCenter(serviceCenter);
        }

        // ✅ Only update image if new image is provided
        if (componentDTO.getImage() != null && !componentDTO.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(componentDTO.getImage(), "components");
            component.setImageUrl(imageUrl);
        }

        BE.entity.Component updatedComponent = componentRepository.save(component);

        ComponentResponse componentResponse = new ComponentResponse();
        modelMapper.map(updatedComponent, componentResponse);

        if (updatedComponent.getServiceCenter() != null) {
            componentResponse.setServiceCenterID(updatedComponent.getServiceCenter().getServiceCenterID());
            componentResponse.setServiceCenterName(updatedComponent.getServiceCenter().getName());
        }

        return componentResponse;
    }

    @Transactional
    public void deleteComponent(Long id) {
        BE.entity.Component component = componentRepository.findByComponentIDAndStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Component not found with id: " + id));
        component.setStatus("inactive");
        componentRepository.save(component);
    }

    @Transactional(readOnly = true)
    public List<ComponentResponse> getAllComponent() {
        return componentRepository.findByStatus("active")
                .stream()
                .map(component -> {
                    ComponentResponse response = new ComponentResponse();
                    modelMapper.map(component, response);
                    if (component.getServiceCenter() != null) {
                        response.setServiceCenterID(component.getServiceCenter().getServiceCenterID());
                        response.setServiceCenterName(component.getServiceCenter().getName());
                    }
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComponentResponse> getComponentByServiceCenter(Long serviceCenterID) {
        ServiceCenter serviceCenter = serviceCenterRepository.findById(serviceCenterID)
                .orElseThrow(() -> new EntityNotFoundException("Service Center not found"));

        return componentRepository.findByServiceCenterAndStatus(serviceCenter, "active")
                .stream()
                .map(component -> {
                    ComponentResponse response = new ComponentResponse();
                    modelMapper.map(component, response);
                    response.setServiceCenterID(serviceCenterID);
                    response.setServiceCenterName(serviceCenter.getName());
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComponentResponse> getComponentByType(String type) {
        return componentRepository.findByTypeAndStatus(type, "active")
                .stream()
                .map(component -> {
                    ComponentResponse response = new ComponentResponse();
                    modelMapper.map(component, response);
                    if (component.getServiceCenter() != null) {
                        response.setServiceCenterID(component.getServiceCenter().getServiceCenterID());
                        response.setServiceCenterName(component.getServiceCenter().getName());
                    }
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComponentResponse> getComponentByName(String name) {
        return componentRepository.findByNameContainingAndStatus(name, "active")
                .stream()
                .map(component -> {
                    ComponentResponse response = new ComponentResponse();
                    modelMapper.map(component, response);
                    if (component.getServiceCenter() != null) {
                        response.setServiceCenterID(component.getServiceCenter().getServiceCenterID());
                        response.setServiceCenterName(component.getServiceCenter().getName());
                    }
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComponentResponse> getLowStockComponent() {
        return componentRepository.findByStatusAndQuantityLessThanEqualMinQuantity("active")
                .stream()
                .map(component -> {
                    ComponentResponse response = new ComponentResponse();
                    modelMapper.map(component, response);
                    if (component.getServiceCenter() != null) {
                        response.setServiceCenterID(component.getServiceCenter().getServiceCenterID());
                        response.setServiceCenterName(component.getServiceCenter().getName());
                    }
                    return response;
                })
                .toList();
    }
}
