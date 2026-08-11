package com.traveltrek.security;

import com.traveltrek.entity.User;
import com.traveltrek.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String authHeader =
                    request.getHeader("Authorization");

            if (authHeader == null ||
                    !authHeader.startsWith("Bearer ")) {

                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            String email = jwtService.extractEmail(token);

            log.info("JWT Email: {}", email);

            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                Optional<User> userOptional =
                        userRepository.findByEmail(email);

                if (userOptional.isPresent()
                        && jwtService.isTokenValid(token, email)) {

                    User user = userOptional.get();

                    var authority =
                            new SimpleGrantedAuthority(
                                    "ROLE_" + user.getRole().name());

                    var authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    null,
                                    List.of(authority));

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.info(
                            "Authenticated user: {} with role {}",
                            user.getEmail(),
                            user.getRole());
                } else {

                    log.warn(
                            "Invalid JWT or user not found for email: {}",
                            email);
                }
            }

        } catch (Exception ex) {

            log.error(
                    "JWT Authentication Error: {}",
                    ex.getMessage(),
                    ex);

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}