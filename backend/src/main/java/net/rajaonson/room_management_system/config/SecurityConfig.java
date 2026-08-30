package net.rajaonson.room_management_system.config;

import net.rajaonson.room_management_system.account.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/accounts/requests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/accounts/requests/validate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/accounts/requests").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/accounts/requests/*/approve").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/accounts/requests/*/refuse").hasRole(Role.ADMIN.name())
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
