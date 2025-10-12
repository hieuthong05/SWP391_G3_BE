package BE.config;

import BE.entity.*;
import BE.model.DTO.VehicleDTO;
import BE.model.request.BookingRequest;
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
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true)
                .setMatchingStrategy(MatchingStrategies.LOOSE);

        modelMapper.createTypeMap(BookingRequest.class, Orders.class)
                .addMappings(mapper -> mapper.skip(Orders::setPaymentStatus));

        return modelMapper;
    }
}