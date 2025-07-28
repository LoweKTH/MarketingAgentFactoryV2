package com.exjobb.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarketingAgentFactoryV2Application {

    public static void main(String[] args) {
        SpringApplication.run(MarketingAgentFactoryV2Application.class, args);
    }

}
