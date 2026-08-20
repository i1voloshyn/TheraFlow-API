package com.theraflow;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {
    @Container
    private static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.4");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return postgres;
    }
}
