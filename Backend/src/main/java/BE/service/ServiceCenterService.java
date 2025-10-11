package BE.service;

import BE.entity.ServiceCenter;
import BE.model.DTO.ServiceCenterDTO;
import BE.model.response.ServiceCenterResponse;
import BE.repository.ServiceCenterRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceCenterService {

    @Autowired
    ServiceCenterRepository serviceCenterRepository;

    @Autowired
    ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public ServiceCenterResponse getServiceCenterById(Long id){
        ServiceCenter serviceCenter = serviceCenterRepository.findByServiceCenterIDAndStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Service Center not found with id: " + id));

        ServiceCenterResponse serviceCenterResponse = new ServiceCenterResponse();
        modelMapper.map(serviceCenter, serviceCenterResponse);

        return serviceCenterResponse;
    }

    @Transactional
    public ServiceCenterResponse createServiceCenter(ServiceCenterDTO serviceCenterDTO) {
        if (serviceCenterRepository.findByEmail(serviceCenterDTO.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email already exists");
        }

        if (serviceCenterRepository.findByPhone(serviceCenterDTO.getPhone()).isPresent()){
            throw new IllegalArgumentException("Phone already exists");
        }

        ServiceCenter serviceCenter = new ServiceCenter();
        modelMapper.map(serviceCenterDTO, serviceCenter);
        serviceCenter.setStatus("active");

        ServiceCenter savedServiceCenter = serviceCenterRepository.save(serviceCenter);
        ServiceCenterResponse serviceCenterResponse = new ServiceCenterResponse();
        modelMapper.map(savedServiceCenter, serviceCenterResponse);

        return serviceCenterResponse;
    }

    @Transactional
    public ServiceCenterResponse updateServiceCenter(Long id, ServiceCenterDTO serviceCenterDTO) {
        ServiceCenter serviceCenter = serviceCenterRepository.findByServiceCenterIDAndStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Service Center not found with id: " + id));

        if (serviceCenterRepository.findByEmailAndServiceCenterIDNot(serviceCenterDTO.getEmail(), id).isPresent()){
            throw new IllegalArgumentException("Email already exists");
        }

        if (serviceCenterRepository.findByPhoneAndServiceCenterIDNot(serviceCenterDTO.getPhone(), id).isPresent()){
            throw new IllegalArgumentException("Phone already exists");
        }

        modelMapper.map(serviceCenterDTO, serviceCenter);

        ServiceCenter updatedServiceCenter = serviceCenterRepository.save(serviceCenter);
        ServiceCenterResponse serviceCenterResponse = new ServiceCenterResponse();
        modelMapper.map(updatedServiceCenter, serviceCenterResponse);

        return serviceCenterResponse;
    }

    @Transactional
    public void deleteServiceCenter(Long id){
        ServiceCenter serviceCenter = serviceCenterRepository.findByServiceCenterIDAndStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Service Center not found with id: " + id));
        serviceCenter.setStatus("inactive");
        serviceCenterRepository.save(serviceCenter);
    }

    @Transactional(readOnly = true)
    public List<ServiceCenterResponse> getAllServiceCenter(){
        return serviceCenterRepository.findByStatus("active")
                .stream()
                .map(serviceCenter -> {
                    ServiceCenterResponse response = new ServiceCenterResponse();
                    modelMapper.map(serviceCenter, response);
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceCenterResponse> getServiceCenterByName(String name){
        return serviceCenterRepository.findByNameContainingAndStatus(name, "active")
                .stream()
                .map(serviceCenter -> {
                    ServiceCenterResponse response = new ServiceCenterResponse();
                    modelMapper.map(serviceCenter, response);
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceCenterResponse> getServiceCenterByLocation(String location){
        return serviceCenterRepository.findByLocationAndStatus(location, "active")
                .stream()
                .map(serviceCenter -> {
                    ServiceCenterResponse response = new ServiceCenterResponse();
                    modelMapper.map(serviceCenter, response);
                    return response;
                })
                .toList();
    }
}
