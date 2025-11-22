package BE.service;

import BE.entity.Customer;
import BE.entity.Model;
import BE.entity.Vehicle;
import BE.model.DTO.VehicleDTO;
import BE.model.response.ModelResponse;
import BE.model.response.VehicleResponse;
import BE.repository.CustomerRepository;
import BE.repository.ModelRepository;
import BE.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class VehicleService {
    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public VehicleResponse createVehicle(VehicleDTO vehicleDTO, MultipartFile image) throws Exception
    {
        if (vehicleRepository.findByLicensePlate(vehicleDTO.getLicensePlate()).isPresent()) {
            throw new IllegalArgumentException("License plate already exists");
        }

        if (vehicleRepository.findByVin(vehicleDTO.getVin()).isPresent()) {
            throw new IllegalArgumentException("VIN already exists");
        }

        Model model = modelRepository.findById(vehicleDTO.getModelID())
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));

        String imageUrl = cloudinaryService.uploadFile(image, "vehicles");

        Vehicle vehicle = modelMapper.map(vehicleDTO, Vehicle.class);

        vehicle.setModel(model);
        vehicle.setImageUrl(imageUrl);

        if (vehicleDTO.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required for a new vehicle.");
        }
        Customer customer = customerRepository.findById(vehicleDTO.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        vehicle.setCustomer(customer);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return mapToVehicleResponse(savedVehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findByVehicleIDAndStatus(id, true)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        return mapToVehicleResponse(vehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        if (vehicleRepository.findByLicensePlateAndVehicleIDNot(dto.getLicensePlate(), id).isPresent()) {
            throw new IllegalArgumentException("License plate already exists");
        }

        if (vehicleRepository.findByVinAndVehicleIDNot(dto.getVin(), id).isPresent()) {
            throw new IllegalArgumentException("VIN already exists");
        }

        modelMapper.map(dto, vehicle);

        if (dto.getModelID() != null) {
            Model model = modelRepository.findById(dto.getModelID())
                    .orElseThrow(() -> new EntityNotFoundException("Model not found"));
            vehicle.setModel(model);
        }

        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
            vehicle.setCustomer(customer);
        }

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        return mapToVehicleResponse(updatedVehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
        vehicle.setExisted(false);
        vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicle() {
        return vehicleRepository.findByStatus(true)
                .stream()
                .map(this::mapToVehicleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        return vehicleRepository.findByCustomerAndExisted(customer, true)
                .stream()
                .map(this::mapToVehicleResponse)
                .toList();
    }

    // Helper method để map Vehicle -> VehicleResponse
    private VehicleResponse mapToVehicleResponse(Vehicle vehicle) {
        VehicleResponse response = modelMapper.map(vehicle, VehicleResponse.class);

        // Map customer
        if (vehicle.getCustomer() != null) {
            response.setCustomerID(vehicle.getCustomer().getCustomerID());
            response.setCustomerName(vehicle.getCustomer().getName() );
        }

        // Map model
        if (vehicle.getModel() != null) {
            response.setModel(modelMapper.map(vehicle.getModel(), ModelResponse.class));
        }

        return response;
    }
}