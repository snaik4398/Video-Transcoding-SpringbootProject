package com.transcodeservice.file.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {
    
    @Value("${storage.minio.endpoint}")
    private String endpoint;
    
    @Value("${storage.minio.access-key}")
    private String accessKey;
    
    @Value("${storage.minio.secret-key}")
    private String secretKey;
    
    @Bean
    public MinioClient minioClient() {
        log.info("Initializing MinIO client with endpoint: {}", endpoint);
        
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
