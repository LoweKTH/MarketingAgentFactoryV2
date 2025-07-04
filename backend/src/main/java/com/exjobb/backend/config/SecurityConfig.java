package com.exjobb.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                // Tillåt åtkomst till actuator och h2-konsolen utan inlogg
                .requestMatchers("/actuator/**", "/h2-console/**", "/api/agent/**") .permitAll()
                // Kräv inloggning för alla andra anrop
                .anyRequest().authenticated()
            )
                // Använd en standard-inloggningssida för de skyddade delarna
                .formLogin(withDefaults())
                // Nödvändiga inställningar för att H2-konsolen ska fungera
                .csrf(csrf -> csrf.ignoringRequestMatchers
                        ("/h2-console/**", "/api/agent/**"))
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));


        return http.build();
    }
}
