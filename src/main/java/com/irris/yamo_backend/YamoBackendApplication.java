package com.irris.yamo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YamoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YamoBackendApplication.class, args);
    }

}
