package com.traveltrek;

import com.traveltrek.entity.User;
import com.traveltrek.entity.UserRole;
import com.traveltrek.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

    private static final Logger log = LoggerFactory.getLogger(TravelTrekApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TravelTrekApplication.class, args);
    }

    /**
     * Seeds default demo/development accounts idempotently on startup.
     */
    @Bean
    public CommandLineRunner initDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed default Travel Agent account
            String agentEmail = "agent@traveltrek.com";
            Optional<User> existingAgent = userRepository.findByEmail(agentEmail);
            if (existingAgent.isEmpty()) {
                User agent = new User(
                        agentEmail,
                        passwordEncoder.encode("Agent@123"),
                        UserRole.TRAVEL_AGENT
                );
                userRepository.save(agent);
                log.info("Default Travel Agent account created: {}", agentEmail);
            } else {
                User agent = existingAgent.get();
                if (!passwordEncoder.matches("Agent@123", agent.getPasswordHash())) {
                    agent.setPasswordHash(passwordEncoder.encode("Agent@123"));
                    agent.setRole(UserRole.TRAVEL_AGENT);
                    agent.setActive(true);
                    userRepository.save(agent);
                    log.info("Default Travel Agent password/role updated: {}", agentEmail);
                }
            }

            // Seed default Travel Manager (AGENCY_MANAGER) account
            String managerEmail = "manager@traveltrek.com";
            Optional<User> existingManager = userRepository.findByEmail(managerEmail);
            if (existingManager.isEmpty()) {
                User manager = new User(
                        managerEmail,
                        passwordEncoder.encode("Manager@123"),
                        UserRole.AGENCY_MANAGER
                );
                userRepository.save(manager);
                log.info("Default Travel Manager account created: {}", managerEmail);
            } else {
                User manager = existingManager.get();
                if (!passwordEncoder.matches("Manager@123", manager.getPasswordHash())) {
                    manager.setPasswordHash(passwordEncoder.encode("Manager@123"));
                    manager.setRole(UserRole.AGENCY_MANAGER);
                    manager.setActive(true);
                    userRepository.save(manager);
                    log.info("Default Travel Manager password/role updated: {}", managerEmail);
                }
            }
        };
    }
}

