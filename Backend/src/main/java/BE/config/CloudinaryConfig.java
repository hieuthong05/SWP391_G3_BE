package BE.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dq5skmidv",
                "api_key", "276544459247457",
                "api_secret", "FtsexMxyqOuJyoZtODwXvKxBFgw",
                "secure", true
        ));
    }
}
