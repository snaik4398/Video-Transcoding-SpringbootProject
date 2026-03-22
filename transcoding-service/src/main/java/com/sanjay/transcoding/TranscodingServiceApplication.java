package com.sanjay.transcoding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.sanjay.common.entity")
@EnableJpaRepositories("com.sanjay.transcoding.repository")
@EnableKafka
@EnableScheduling
@ComponentScan(basePackages = {"com.sanjay.transcoding", "com.sanjay.common"})
public class TranscodingServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TranscodingServiceApplication.class, args);
    }
}
