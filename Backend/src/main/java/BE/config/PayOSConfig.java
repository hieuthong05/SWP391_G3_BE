package BE.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Sửa import này để dùng package gốc
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Bean
    public PayOS payOS() {
        // Sửa lại cách khởi tạo, dùng constructor trực tiếp như trong doc v1
        return new PayOS(this.clientId, this.apiKey, this.checksumKey);
    }
}