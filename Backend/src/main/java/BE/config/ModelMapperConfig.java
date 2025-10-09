package BE.config;

import BE.entity.Admin;
import BE.entity.Customer;
import BE.entity.Employee;
import BE.entity.Vehicle;
import BE.model.VehicleDTO;
import BE.model.response.UserResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper(){

        ModelMapper modelMapper = new ModelMapper();

//        modelMapper.createTypeMap(VehicleDTO.class, Vehicle.class)
//                .addMappings(mapper -> {
//                    mapper.map(src -> null, Vehicle::setCustomer);
//                    mapper.skip(Vehicle::setVehicleID);
//                });

        // Configure to skip null values
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);


        return modelMapper;
    }
}
