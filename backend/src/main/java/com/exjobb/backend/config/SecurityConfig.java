package com.exjobb.backend.config;

import com.exjobb.backend.utils.JwtAuthenticationFilter;
import com.exjobb.backend.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Remove the UserService dependency from constructor
    // Remove the @Autowired JwtAuthenticationFilter field

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        // Tillåt åtkomst till actuator, h2-konsolen och login utan inlogg
                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/facebook/**").permitAll()
                        // Rollbaserad åtkomst
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        // Kräv inloggning för alla andra anrop
                        .anyRequest().authenticated()
                )
                // Inaktivera form login för JWT
                .formLogin(form -> form.disable())
                // Inaktivera HTTP Basic auth
                .httpBasic(basic -> basic.disable())
                // Stateless session för JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // CORS konfiguration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF inställningar för H2-konsolen och API
                .csrf(csrf -> csrf.ignoringRequestMatchers
                        ("/h2-console/**", "/api/agent/**", "/api/auth/**", "/api/chat/**"))
                // Nödvändiga inställningar för att H2-konsolen ska fungera
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                // Lägg till JWT filter - now injected as parameter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder bean required by UserService.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}