
package com.mms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // PUBLIC PAGES AND STATIC RESOURCES
                .requestMatchers(HttpMethod.GET, "/", "/login", "/merchants", "/payments", "/settlements", "/reports", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // ADMIN-ONLY OPERATIONS
                .requestMatchers(
                    HttpMethod.POST,
                    "/merchants/add",
                    "/merchants/edit",
                    "/merchants/status",
                    "/merchants/delete",
                    "/payments/add",
                    "/payments/complete",
                    "/payments/fail",
                    "/settlements/add"
                ).hasRole("ADMIN")

                // ALL OTHER REQUESTS REQUIRE LOGIN
                .anyRequest().authenticated()
            )

            // CUSTOM ADMIN LOGIN
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .failureUrl("/login?error")
                .defaultSuccessUrl("/", false)
                .permitAll()
            )

            // ADMIN LOGOUT
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    // ADMIN USER DETAILS
    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder) {

        String username =
            System.getenv("MMS_ADMIN_USERNAME");

        String password =
            System.getenv("MMS_ADMIN_PASSWORD");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalStateException(
                "Set MMS_ADMIN_USERNAME and MMS_ADMIN_PASSWORD"
            );
        }

        return new InMemoryUserDetailsManager(
            User.withUsername(username)
                .password(encoder.encode(password))
                .roles("ADMIN")
                .build()
        );
    }

    // PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
            .createDelegatingPasswordEncoder();
    }
}
