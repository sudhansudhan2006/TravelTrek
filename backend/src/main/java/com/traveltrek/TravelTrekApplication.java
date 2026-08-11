package com.traveltrek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TravelTrekApplication - Entry Point
 *
 * Purpose: This is the starting class of the entire Spring Boot application.
 * Why it exists: Every Spring Boot project needs one class with @SpringBootApplication
 *                to bootstrap the application context, component scanning, and auto-configuration.
 * How it works: SpringApplication.run() starts the embedded Tomcat server and
 *               initializes all beans (controllers, services, repositories, etc.).
 */
@SpringBootApplication
public class TravelTrekApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelTrekApplication.class, args);
    }
}
