package com.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.backend.repository.AdminRepository;
import com.backend.repository.AdvisorRepository;
import com.backend.repository.CustomerRepository;
import com.backend.service.TokenBlacklistService;
import com.frontend.controller.AdminController;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final AdvisorRepository advisorRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(AdminRepository adminRepository,
                          CustomerRepository customerRepository,
                          AdvisorRepository advisorRepository,
                          JwtUtil jwtUtil,
                          TokenBlacklistService tokenBlacklistService) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.advisorRepository = advisorRepository;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/service-packages").permitAll()
                .requestMatchers("/api/service-centers").permitAll()
                .requestMatchers("/api/vehicle-types").permitAll()
                .requestMatchers("/api/vehicle-models").permitAll()
                .requestMatchers(HttpMethod.POST, "/bookings/{id}/assign-advisor").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/bookings/{id}/complete").hasRole("SERVICE_ADVISOR")
                .requestMatchers("/api/service-centers/**").hasRole("ADMIN")
                .requestMatchers("/api/requests/**").hasAnyRole("CUSTOMER", "SERVICE_ADVISOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole("ADMIN", "SERVICE_ADVISOR")
                .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers("/api/inventory/use/**").hasRole("SERVICE_ADVISOR")
                .requestMatchers("/api/payments/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/locations/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/customer").hasRole("CUSTOMER")
                .requestMatchers("/api/dashboard/service-advisor").hasRole("SERVICE_ADVISOR")
                .requestMatchers(HttpMethod.GET, "/api/advisor/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/advisor/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/advisor/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/advisor/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/service-advisor/**").hasRole("SERVICE_ADVISOR")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/api/vehicle-type/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers("/api/vehicle-models/**").hasRole("ADMIN")
                .requestMatchers("/api/customers/**").hasRole("ADMIN")
                .requestMatchers("/api/advisors/**").hasRole("ADMIN")
                .requestMatchers("/api/service-packages/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, tokenBlacklistService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8082"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        logger.info("CORS configuration: allowedOrigins={}, allowedMethods={}, allowedHeaders={}",
            configuration.getAllowedOrigins(), configuration.getAllowedMethods(), configuration.getAllowedHeaders());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return identifier -> {
            return adminRepository.findByUsername(identifier)
                .map(u -> {
                    String role = u.getRole().name();
                    logger.info("Admin role for {}: {}", identifier, role);
                    return org.springframework.security.core.userdetails.User
                        .withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(role)
                        .build();
                })
                .or(() -> customerRepository.findByUsername(identifier)
                    .map(u -> {
                        String role = u.getRole().name();
                        logger.info("Customer role for {}: {}", identifier, role);
                        return org.springframework.security.core.userdetails.User
                            .withUsername(u.getUsername())
                            .password(u.getPassword())
                            .roles(role)
                            .build();
                    }))
                .or(() -> advisorRepository.findByUsername(identifier)
                    .map(u -> {
                        String role = u.getRole().name();
                        logger.info("Advisor role for {}: {}", identifier, role);
                        return org.springframework.security.core.userdetails.User
                            .withUsername(u.getUsername())
                            .password(u.getPassword())
                            .roles(role)
                            .build();
                    }))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        };
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}