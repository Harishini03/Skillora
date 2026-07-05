package com.placement.placement_intelligence.config;

import com.placement.placement_intelligence.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Security headers
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(xss -> xss
                                .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                        "font-src 'self' https://fonts.gstatic.com; " +
                                        "img-src 'self' data: https:; " +
                                        "connect-src 'self' https://api.groq.com;"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                )
                
                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**", "/public/**").permitAll()
                        
                        // Authentication endpoints (public)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Legacy auth routes (not under /api prefix)
                        .requestMatchers("/login", "/signup", "/google-login").permitAll()
                        
                        // Student endpoints
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        
                        // Staff/Admin endpoints
                        .requestMatchers("/api/staff/**").hasRole("STAFF")
                        
                        // Recruiter endpoints
                        .requestMatchers("/api/recruiter/**").hasRole("RECRUITER")
                        
                        // Shared endpoints (analytics, reports accessible by STAFF and RECRUITER)
                        .requestMatchers("/api/shared/analytics/**").hasAnyRole("STAFF", "RECRUITER")

                        // Analytics endpoints — student can view their own data; STAFF/RECRUITER see everything
                        .requestMatchers("/api/analytics/student/**").hasAnyRole("STUDENT", "STAFF", "RECRUITER")
                        .requestMatchers("/api/analytics/**").hasAnyRole("STAFF", "RECRUITER")

                        // Common endpoints (accessible by all authenticated users)
                        .requestMatchers("/api/common/**").authenticated()
                        
                        // Course and Coding platform (accessible by STUDENT)
                        .requestMatchers("/api/courses/**", "/api/coding/**", "/api/code/**").hasRole("STUDENT")
                        
                        // Job and interview endpoints
                        .requestMatchers("/api/jobs/**").authenticated()
                        .requestMatchers("/api/applications/**").hasAnyRole("STUDENT", "RECRUITER")
                        .requestMatchers("/api/interviews/**").hasAnyRole("RECRUITER", "STAFF")
                        
                        // AI Mentor (STUDENT only)
                        .requestMatchers("/api/ai/**", "/api/ai-mentor/**").hasRole("STUDENT")

                        // Notifications (all authenticated users)
                        .requestMatchers("/api/notifications/send-to-all").hasAnyRole("STAFF","RECRUITER")
                        .requestMatchers("/api/notifications/**").authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Increased strength from default 10 to 12
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
