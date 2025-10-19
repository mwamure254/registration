package com.mfano.registration.security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  private boolean enabled = false;

  private String fullName;
  private String username = email;

  @Enumerated(EnumType.STRING)
  private Role role = Role.USER;

  private LocalDateTime createdAt = LocalDateTime.now();

  public User() {}

  // getters and setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public Role getRole() { return role; }
  public void setRole(Role role) { this.role = role; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

}
