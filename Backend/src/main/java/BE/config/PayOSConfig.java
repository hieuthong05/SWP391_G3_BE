package BE.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Sửa import này để dùng package gốc
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
public class PayOSConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;
    private final String PAYOS_SANDBOX_URL = "https://api-merchant.payos.vn/";
    @Bean
    public PayOS payOS() {
        // Sửa lại cách khởi tạo, dùng constructor trực tiếp như trong doc v1
        ClientOptions options = ClientOptions.builder()
                .clientId(this.clientId)
                .apiKey(this.apiKey)
                .checksumKey(this.checksumKey)
                .baseURL(PAYOS_SANDBOX_URL) // <--- CHỈ ĐỊNH RÕ MÔI TRƯỜNG SANDBOX
                .build();

        return new PayOS(options);
    }
}