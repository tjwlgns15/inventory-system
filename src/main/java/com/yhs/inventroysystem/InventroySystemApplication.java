package com.yhs.inventroysystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InventroySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventroySystemApplication.class, args);
    }

}
