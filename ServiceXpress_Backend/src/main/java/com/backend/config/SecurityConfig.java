package com.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.repository.AdminRepository;
import com.backend.repository.CustomerRepository;
import com.backend.repository.ServiceAdvisorRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final ServiceAdvisorRepository serviceAdvisorRepository;
    private final JwtUtil jwtUtil;

    public SecurityConfig(AdminRepository adminRepository,
                         CustomerRepository customerRepository,
                         ServiceAdvisorRepository serviceAdvisorRepository,
                         JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.serviceAdvisorRepository = serviceAdvisorRepository;
        this.jwtUtil = jwtUtil;
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
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/customer").hasRole("CUSTOMER")
                .requestMatchers("/api/dashboard/service-advisor").hasRole("SERVICE_ADVISOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService()), 
                           UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*"); // Adjust for production
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return identifier -> {
            System.out.println("Attempting to load user: " + identifier);
            return adminRepository.findByUsername(identifier)
                .map(u -> {
                    System.out.println("Found admin: " + u.getUsername() + ", password: " + u.getPassword());
                    return org.springframework.security.core.userdetails.User
                        .withUsername(u.getUsername())
                        .password("{noop}" + u.getPassword()) // {noop} for already encoded password
                        .roles(u.getRole().name())
                        .build();
                })
                .or(() -> customerRepository.findByUsername(identifier)
                    .map(u -> {
                        System.out.println("Found customer: " + u.getUsername());
                        return org.springframework.security.core.userdetails.User
                            .withUsername(u.getUsername())
                            .password("{noop}" + u.getPassword())
                            .roles(u.getRole().name())
                            .build();
                    }))
                .or(() -> serviceAdvisorRepository.findByUsername(identifier)
                    .map(u -> {
                        System.out.println("Found service advisor: " + u.getUsername());
                        return org.springframework.security.core.userdetails.User
                            .withUsername(u.getUsername())
                            .password("{noop}" + u.getPassword())
                            .roles(u.getRole().name())
                            .build();
                    }))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPasswordAdvisor = "advisor123";
        String newHashAdvisor = encoder.encode(plainPasswordAdvisor);
        System.out.println("Newly generated hash for 'advisor123': " + newHashAdvisor);
        boolean matchesAdvisor = encoder.matches(plainPasswordAdvisor, newHashAdvisor);
        System.out.println("Does 'advisor123' match the new advisor hash? " + matchesAdvisor);

        String plainPasswordAdmin = "admin123";
        String newHashAdmin = encoder.encode(plainPasswordAdmin);
        System.out.println("Newly generated hash for 'admin123': " + newHashAdmin);
        boolean matchesAdmin = encoder.matches(plainPasswordAdmin, newHashAdmin);
        System.out.println("Does 'admin123' match the new admin hash? " + matchesAdmin);

        String plainPasswordCustomer = "customer123";
        String newHashCustomer = encoder.encode(plainPasswordCustomer);
        System.out.println("Newly generated hash for 'customer123': " + newHashCustomer);
        boolean matchesCustomer = encoder.matches(plainPasswordCustomer, newHashCustomer);
        System.out.println("Does 'customer123' match the new customer hash? " + matchesCustomer);
    }
    
    
}