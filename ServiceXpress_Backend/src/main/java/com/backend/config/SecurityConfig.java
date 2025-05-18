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
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/service-packages/**").permitAll()
                .requestMatchers("/api/service-centers").permitAll()
                .requestMatchers("/api/vehicle-types/**").permitAll()
                .requestMatchers("/api/vehicle-models/**").permitAll()
                .requestMatchers("/api/bookings/**").permitAll()
                .requestMatchers("/api/customer/**").permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/bookings/**").hasRole("ADMIN")
                .requestMatchers("/dashboard/admin").hasRole("ADMIN") 
               
                // Advisor management (admin-only)
                .requestMatchers(HttpMethod.GET, "/api/advisor/**").hasRole("ADMIN") 
                .requestMatchers(HttpMethod.POST, "/api/advisor/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/advisor/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/advisor/**").hasRole("ADMIN") 

                // Inventory management
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole("ADMIN", "SERVICE_ADVISOR")
                .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasRole("ADMIN") 
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN") 


                // Role-based endpoints
                .requestMatchers("/api/requests/**").hasAnyRole("CUSTOMER", "SERVICE_ADVISOR", "ADMIN") // Request management
                .requestMatchers("/dashboard/customer").hasRole("CUSTOMER") // Customer dashboard page
                .requestMatchers("/dashboard/advisor").hasRole("SERVICE_ADVISOR") // Advisor dashboard redirect
                .requestMatchers("/api/advisors/**").hasAnyRole("SERVICE_ADVISOR", "ADMIN") // Advisor-related APIs (e.g., assigned vehicles)
                .requestMatchers("/service-advisor/**").hasRole("SERVICE_ADVISOR")
                .requestMatchers("/api/customer/me").hasRole("CUSTOMER")

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
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}