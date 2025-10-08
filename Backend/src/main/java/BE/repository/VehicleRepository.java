package BE.repository;

import BE.entity.Customer;
import BE.entity.Vehicle;
import BE.model.VehicleDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle,Long> {
    List<Vehicle> findByCustomerCustomerID(Long customerId);
    Optional<Vehicle> findByLicensePlateAndVehicleIDNot(String licensePlate, Long vehicleID);
    Optional<Vehicle> findByVehicleIDAndStatus(Long id,Boolean status);
    Optional<Vehicle> findByVinAndVehicleIDNot(String VIN, Long vehicleID);
    List<Vehicle> findByCustomerAndStatus(Customer customer, Boolean status);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByVin(String vin);
    List<Vehicle> findByStatus(Boolean status);
}
