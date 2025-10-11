package BE.service;

import BE.entity.Service;
import BE.model.DTO.ServiceDTO;
import BE.model.response.ServiceResponse;
import BE.repository.ServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServicesService {

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id){
        BE.entity.Service service = serviceRepository.findByServiceIDAndServiceStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Service not found with id: " + id));

        ServiceResponse serviceResponse = new ServiceResponse();
        modelMapper.map(service, serviceResponse);

        return serviceResponse;
    }

    @Transactional
    public ServiceResponse createService(ServiceDTO serviceDTO) {
        BE.entity.Service service = new Service();
        modelMapper.map(serviceDTO, service);
        service.setServiceID(null);
        service.setServiceStatus("active");

        BE.entity.Service savedService = serviceRepository.save(service);
        ServiceResponse serviceResponse = new ServiceResponse();
        modelMapper.map(savedService, serviceResponse);

        return serviceResponse;
    }

    @Transactional
    public ServiceResponse updateService(Long id, ServiceDTO serviceDTO) {
        BE.entity.Service service = serviceRepository.findByServiceIDAndServiceStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Service not found with id: " + id));

        modelMapper.map(serviceDTO, service);

        BE.entity.Service updatedService = serviceRepository.save(service);
        ServiceResponse serviceResponse = new ServiceResponse();
        modelMapper.map(updatedService, serviceResponse);

        return serviceResponse;
    }

    @Transactional
    public void deleteService(Long id){
        BE.entity.Service service = serviceRepository.findByServiceIDAndServiceStatus(id, "active")
                .orElseThrow(() -> new EntityNotFoundException("Service not found with id: " + id));
        service.setServiceStatus("inactive");
        serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllService(){
        return serviceRepository.findByServiceStatus("active")
                .stream()
                .map(service -> {
                    ServiceResponse response = new ServiceResponse();
                    modelMapper.map(service, response);
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServiceByType(String serviceType){
        return serviceRepository.findByServiceTypeAndServiceStatus(serviceType, "active")
                .stream()
                .map(service -> {
                    ServiceResponse response = new ServiceResponse();
                    modelMapper.map(service, response);
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServiceByName(String serviceName){
        return serviceRepository.findByServiceNameContainingAndServiceStatus(serviceName, "active")
                .stream()
                .map(service -> {
                    ServiceResponse response = new ServiceResponse();
                    modelMapper.map(service, response);
                    return response;
                })
                .toList();
    }

    public List<Map<String, Object>> getMostBookedServices() {
        List<Object[]> results = serviceRepository.findMostBookedServices();
        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("service", result[0]);
                    map.put("orderCount", result[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }
}
