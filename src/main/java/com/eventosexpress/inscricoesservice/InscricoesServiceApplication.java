package com.eventosexpress.inscricoesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.eventosexpress.inscricoesservice.client")
@SpringBootApplication
public class InscricoesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InscricoesServiceApplication.class, args);
    }

}
