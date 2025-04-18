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

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                // Public access
                .requestMatchers("/api/auth/**").permitAll() // Auth endpoints open for everyone
                
                // Admin specific access
                .requestMatchers("/api/service-centers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole("ADMIN", "SERVICE_ADVISOR") // Read-only access for admins and service advisors
                .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers("/api/advisors/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                .requestMatchers("/api/locations/**").hasRole("ADMIN")

                // Customer and Admin access to dashboard
                .requestMatchers("/api/dashboard/customer").hasRole("CUSTOMER")
                .requestMatchers("/api/payments/**").hasAnyRole("CUSTOMER", "ADMIN") // Payments accessible to both Customer and Admin

                // Service Advisor access to inventory usage and dashboard
                .requestMatchers("/api/inventory/use/**").hasRole("SERVICE_ADVISOR")
                .requestMatchers("/api/dashboard/service-advisor").hasRole("SERVICE_ADVISOR")
                
                // Access for all roles to view resources
                .requestMatchers("/api/requests/**").hasAnyRole("CUSTOMER", "SERVICE_ADVISOR", "ADMIN") // Requests are available for all roles
                .requestMatchers("/api/advisors/**").hasRole("ADMIN") // Admin controls advisors
                
                // Generic access for all authenticated users
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/service-advisor/**").hasRole("SERVICE_ADVISOR")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")

                // Any other request needs authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8082");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
