package com.traveltrek.config;

import com.traveltrek.security.CustomAccessDeniedHandler;
import com.traveltrek.security.CustomAuthEntryPoint;
import com.traveltrek.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            CustomAuthEntryPoint customAuthEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Frontend static resources and pages
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/dashboard.html",
                                "/bookings.html",
                                "/activities.html",
                                "/destinations.html",
                                "/itinerary.html",
                                "/packages.html",
                                "/profile.html",
                                "/reports.html",
                                "/users.html",
                                "/unauthorized.html",
                                "/*.html",
                                "/css/**",
                                "/js/**",
                                "/assets/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/packages/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/packages/**").hasRole("AGENCY_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/packages/**").hasRole("AGENCY_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/packages/**").hasRole("AGENCY_MANAGER")

                        .requestMatchers("/api/v1/destinations/**").hasRole("AGENCY_MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/activities/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/activities/**")
                        .hasAnyRole("TRAVEL_AGENT", "AGENCY_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/activities/**")
                        .hasAnyRole("TRAVEL_AGENT", "AGENCY_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/activities/**")
                        .hasAnyRole("TRAVEL_AGENT", "AGENCY_MANAGER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings").hasRole("TRAVELER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings")
                        .hasAnyRole("TRAVEL_AGENT", "AGENCY_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/*/cancel")
                        .hasAnyRole("TRAVELER", "AGENCY_MANAGER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/itineraries").hasRole("TRAVELER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/itineraries/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/itineraries/all")
                        .hasAnyRole("TRAVEL_AGENT", "AGENCY_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/itineraries/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/itineraries/*")
                        .hasAnyRole("TRAVELER", "AGENCY_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/itineraries/*")
                        .hasAnyRole("TRAVELER", "AGENCY_MANAGER")

                        .requestMatchers("/api/v1/profile/**").authenticated()
                        .requestMatchers("/api/v1/users/**").hasRole("AGENCY_MANAGER")
                        .requestMatchers("/api/v1/reports/**").hasRole("AGENCY_MANAGER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        java.util.List<String> allowedOrigins = new java.util.ArrayList<>(List.of(
                "http://127.0.0.1",
                "http://localhost",
                "http://127.0.0.1:8080",
                "http://localhost:8080",
                "http://127.0.0.1:5500",
                "http://localhost:5500",
                "http://localhost:63342",
                "https://traveltrek-dun.vercel.app"
        ));

        String customOrigin = System.getenv("CORS_ALLOWED_ORIGIN");
        if (customOrigin != null && !customOrigin.trim().isEmpty() && !allowedOrigins.contains(customOrigin.trim())) {
            allowedOrigins.add(customOrigin.trim());
        }

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}