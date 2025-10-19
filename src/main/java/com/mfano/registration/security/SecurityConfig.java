package com.mfano.registration.security;

import com.mfano.registration.security.model.User;
import com.mfano.registration.security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserRepository userRepository;

  @Bean
  public UserDetailsService userDetailsService() {
    return email -> {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("User not found"));
      if (!user.isEnabled())
        throw new UsernameNotFoundException("Email not verified");
      return org.springframework.security.core.userdetails.User.builder()
          .username(user.getUsername())
          .password(user.getPassword())
          .roles(user.getRole().name())
          .build();
    };
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/register", "/login", "/verify", "/forgot", "/reset-password", "/resend", "/error",
            "/css/**",
            "/js/**")
        .permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .requestMatchers("/director/**").hasRole("DIRECTOR")
        .requestMatchers("/hoi/**").hasRole("HOI")
        .requestMatchers("/user/**").hasRole("USER")
        .requestMatchers("/profile").permitAll()
        .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/redirect", true)
            .permitAll())
        .exceptionHandling(handling -> handling.accessDeniedPage("/error"))
        .logout(logout -> logout.logoutSuccessUrl("/login").permitAll());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

    provider.setUserDetailsService(userDetailsService());

    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

}
