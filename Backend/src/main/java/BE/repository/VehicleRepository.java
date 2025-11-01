package BE.repository;

import BE.entity.Customer;
import BE.entity.Vehicle;
import BE.model.DTO.VehicleDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle,Long> {
    List<Vehicle> findByCustomerCustomerID(Long customerId);
    Optional<Vehicle> findByLicensePlateAndVehicleIDNot(String licensePlate, Long vehicleID);
    Optional<Vehicle> findByVehicleIDAndStatus(Long id,Boolean status);
    Optional<Vehicle> findByVinAndVehicleIDNot(String VIN, Long vehicleID);
    List<Vehicle> findByCustomerAndStatus(Customer customer, Boolean status);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByVin(String vin);
    List<Vehicle> findByStatus(Boolean status);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = true")
    long countActiveVehicles();
}
