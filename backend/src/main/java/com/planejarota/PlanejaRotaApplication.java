package com.planejarota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PlanejaRotaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlanejaRotaApplication.class, args);
    }
}
