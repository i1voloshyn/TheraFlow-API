package com.theraflow;

import org.springframework.boot.SpringApplication;

public class TestTheraFlowApplication {

    public static void main(String[] args) {
        SpringApplication.from(TheraFlowApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
