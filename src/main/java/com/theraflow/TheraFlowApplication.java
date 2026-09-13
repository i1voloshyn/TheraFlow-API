package com.theraflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TheraFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheraFlowApplication.class, args);
    }

}
