package BE.service;

import BE.entity.Customer;
import BE.entity.Vehicle;
import BE.model.DTO.VehicleDTO;
import BE.model.response.VehicleResponse;
import BE.repository.CustomerRepository;
import BE.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {
    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    public VehicleResponse createVehicle(VehicleDTO vehicleDTO) {
        if (vehicleRepository.findByLicensePlate(vehicleDTO.getLicensePlate()).isPresent()){
            throw new IllegalArgumentException("License plate already exists");
        }

        if (vehicleRepository.findByVin(vehicleDTO.getVin()).isPresent()){
            throw new IllegalArgumentException("VIN already exists");
        }

        Vehicle vehicle = new Vehicle();
        modelMapper.map(vehicleDTO, vehicle);
        vehicle.setVehicleID(null);

        if (vehicleDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(vehicleDTO.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
            vehicle.setCustomer(customer);
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        VehicleResponse vehicleResponse = new VehicleResponse();
        modelMapper.map(savedVehicle, vehicleResponse);

        return vehicleResponse;
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id){
        Vehicle vehicle = vehicleRepository.findByVehicleIDAndStatus(id, true)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        VehicleResponse vehicleResponse = new VehicleResponse();
        modelMapper.map(vehicle, vehicleResponse);

        return vehicleResponse;
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        if (vehicleRepository.findByLicensePlateAndVehicleIDNot(dto.getLicensePlate(), id).isPresent()){
            throw new IllegalArgumentException("License plate already exists");
        }

        if (vehicleRepository.findByVinAndVehicleIDNot(dto.getVin(), id).isPresent()){
            throw new IllegalArgumentException("VIN already exists");
        }

        modelMapper.map(dto, vehicle);

        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
            vehicle.setCustomer(customer);
        }

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        VehicleResponse vehicleResponse = new VehicleResponse();
        modelMapper.map(updatedVehicle, vehicleResponse);

        return vehicleResponse;
    }

    @Transactional
    public void deleteVehicle(Long id){
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
        vehicle.setStatus(false);
        vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicle(){
        return vehicleRepository.findByStatus(true)
                .stream()
                .map(vehicle -> {
                    VehicleResponse response = new VehicleResponse();
                    modelMapper.map(vehicle, response);
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCustomerId(Long customerId){
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        return vehicleRepository.findByCustomerAndStatus(customer, true)
                .stream()
                .map(vehicle -> {
                    VehicleResponse response = new VehicleResponse();
                    modelMapper.map(vehicle, response);
                    return response;
                })
                .toList();
    }
}
