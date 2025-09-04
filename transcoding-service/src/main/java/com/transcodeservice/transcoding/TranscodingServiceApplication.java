package com.transcodeservice.transcoding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EntityScan("com.transcodeservice.common.entity")
@EnableJpaRepositories("com.transcodeservice.transcoding.repository")
@EnableKafka
public class TranscodingServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TranscodingServiceApplication.class, args);
    }
}
