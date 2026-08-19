package com.theraflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TheraFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheraFlowApplication.class, args);
    }

}
