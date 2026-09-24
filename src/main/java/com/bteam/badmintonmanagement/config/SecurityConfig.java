package com.bteam.badmintonmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }


    private static final String[]  PUBLIC_URLS= {
            "/auth/register",
            "/auth/login",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/error"
    };

    private static final String[] MANGAER_URLS={
            // endpoint chỉ manager gọi
    };



    private static final String[]  STAFF_URLS= {
            // endpoint QUYỀN THẤP NHẤT LÀ STAFF GỌI
    };

    private static final String[]  CUSTOMER_URLS= {
            // endpoint QUYỀN THẤP NHẤT LÀ CUSTOMER GỌI
    };


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception
    {
        http
                .csrf(csrf->csrf.disable())

                //Basic auth mỗi request phải có tk mk
                .sessionManagement(session->
                        session.sessionCreationPolicy( SessionCreationPolicy.STATELESS))

                //Phân quyền
                .authorizeHttpRequests(auth->auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(MANGAER_URLS).hasRole("MANAGER")
                        .requestMatchers(STAFF_URLS).hasAnyRole("MANAGER","STAFF")
                        .requestMatchers(CUSTOMER_URLS).hasAnyRole("MANAGER","STAFF","CUSTOMER")
                        .anyRequest().authenticated())

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
