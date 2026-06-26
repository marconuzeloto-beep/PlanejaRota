package com.logicore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LogiCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogiCoreApplication.class, args);
    }
}
