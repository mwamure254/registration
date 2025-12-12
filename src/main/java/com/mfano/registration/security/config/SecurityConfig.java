package com.mfano.registration.security.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.mfano.registration.security.service.CustomUserDetailsService;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/admin/**", "/profile").hasRole("ADMIN")
        .requestMatchers("/director/**", "/profile").hasRole("DIRECTOR")
        .requestMatchers("/hoi/**", "/profile").hasRole("HOI")
        .requestMatchers("/user/**", "/profile").hasRole("USER")
        .requestMatchers("/", "/register", "/login", "/verify", "/forgot", "/reset-password", "/resend",
            "/error", "/css/**", "/js/**")
        .permitAll()
        .anyRequest().authenticated())

        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard", true)
            // .successForwardUrl("/dashboard")
            .permitAll())

        .exceptionHandling(handling -> handling.accessDeniedPage("/error-page"))
        .logout(logout -> logout.invalidateHttpSession(true).clearAuthentication(true)
            .logoutSuccessUrl("/login").permitAll())

        .sessionManagement(management -> management
            .maximumSessions(1)
            .expiredUrl("/login?expired=true"));

    return http.build();
  }
  

  @Bean
  HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return new CustomUserDetailsService();
  }

  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

    provider.setUserDetailsService(userDetailsService());
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

}
