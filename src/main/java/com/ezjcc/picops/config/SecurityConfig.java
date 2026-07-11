package com.ezjcc.picops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/signup", "/activate", "/css/**", "/js/**", "/error").permitAll()
                // public-album views; controllers enforce per-album visibility,
                // continuing the 2005 thesis: access is mediated by the app, not the URL
                .requestMatchers(HttpMethod.GET, "/albums/*", "/pictures/**", "/u/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?loggedout")
                .permitAll());
        return http.build();
    }

    /**
     * Delegating encoder: bcrypt today, argon2 tomorrow without invalidating
     * stored hashes (each hash is prefixed with its algorithm id).
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
