package com.backend.config;

import java.util.Arrays;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final AdvisorRepository advisorRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

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
                // Public endpoints (no authentication required)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll() // Authentication endpoints
                .requestMatchers("/api/service-packages/**").permitAll() // Service packages lookup
                .requestMatchers("/api/service-centers").permitAll() // Service centers lookup
                .requestMatchers("/api/vehicle-types/**").permitAll() // Vehicle types lookup
                .requestMatchers("/api/vehicle-models/**").permitAll() // Vehicle models lookup
                .requestMatchers("/api/bookings/**").permitAll() // Booking-related endpoints
                .requestMatchers("/api/customer/**").permitAll() // Customer-related endpoints
                .requestMatchers("/login").permitAll() // Login page
                .requestMatchers("/").permitAll() // Home page

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // General admin APIs
                .requestMatchers("/api/admin/bookings/**").hasRole("ADMIN") // Admin booking management
                .requestMatchers("/api/service-centers/**").hasRole("ADMIN") // Service center management
                .requestMatchers("/api/locations/**").hasRole("ADMIN") // Location management
                .requestMatchers("/api/dashboard/admin").hasRole("ADMIN") // Admin dashboard API
                .requestMatchers("/dashboard/admin").hasRole("ADMIN") // Admin dashboard page
                .requestMatchers("/admin/**").hasRole("ADMIN") // Admin web pages
                .requestMatchers("/inventory").hasRole("ADMIN") // Inventory page
                .requestMatchers("/service-centers").hasRole("ADMIN") // Service centers page
                .requestMatchers("/customers").hasRole("ADMIN") // Customers page
                .requestMatchers("/advisors").hasRole("ADMIN") // Advisors page
                .requestMatchers("/service-package").hasRole("ADMIN") // Service package page
                .requestMatchers("/vehicle-model").hasRole("ADMIN") // Vehicle model page
                .requestMatchers("/vehicle-type").hasRole("ADMIN") // Vehicle type page

                // Advisor management (admin-only)
                .requestMatchers(HttpMethod.GET, "/api/advisor/**").hasRole("ADMIN") // View advisors
                .requestMatchers(HttpMethod.POST, "/api/advisor/**").hasRole("ADMIN") // Create advisors
                .requestMatchers(HttpMethod.PUT, "/api/advisor/**").hasRole("ADMIN") // Update advisors
                .requestMatchers(HttpMethod.DELETE, "/api/advisor/**").hasRole("ADMIN") // Delete advisors

                // Inventory management
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole("ADMIN", "SERVICE_ADVISOR") // View inventory
                .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasRole("ADMIN") // Create inventory
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("ADMIN") // Update inventory
                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN") // Delete inventory
                .requestMatchers("/api/inventory/use/**").hasRole("SERVICE_ADVISOR") // Use inventory

                // Role-based endpoints
                .requestMatchers("/api/requests/**").hasAnyRole("CUSTOMER", "SERVICE_ADVISOR", "ADMIN") // Request management
                .requestMatchers("/api/payments/**").hasAnyRole("CUSTOMER", "ADMIN") // Payment management
                .requestMatchers("/api/dashboard/customer").hasRole("CUSTOMER") // Customer dashboard API
                .requestMatchers("/dashboard/customer").hasRole("CUSTOMER") // Customer dashboard page
                .requestMatchers("/api/dashboard/advisor/**").hasRole("SERVICE_ADVISOR") // Advisor dashboard API
                .requestMatchers("/dashboard/advisor").hasRole("SERVICE_ADVISOR") // Advisor dashboard redirect
                .requestMatchers("/api/advisors/**").hasAnyRole("SERVICE_ADVISOR", "ADMIN") // Advisor-related APIs (e.g., assigned vehicles)
                .requestMatchers("/service-advisor/**").hasRole("SERVICE_ADVISOR") // Advisor web pages
                .requestMatchers("/customer/**").hasRole("CUSTOMER") // Customer web pages
                .requestMatchers("/api/vehicle-type/**").hasAnyRole("ADMIN", "CUSTOMER") // Vehicle type management

                // Fallback rule for all other requests
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
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return identifier -> {
            return adminRepository.findByUsername(identifier)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole().name())
                        .build())
                .or(() -> customerRepository.findByUsername(identifier)
                    .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole().name())
                        .build()))
                .or(() -> advisorRepository.findByUsername(identifier)
                    .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole().name())
                        .build()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}