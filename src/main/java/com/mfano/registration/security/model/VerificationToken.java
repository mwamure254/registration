package com.mfano.registration.security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class VerificationToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  private LocalDateTime expiryDate;

  public VerificationToken() {
  }

  public VerificationToken(String token, User user, LocalDateTime expiryDate) {
    this.token = token;
    this.user = user;
    this.expiryDate = expiryDate;
  }

  public Long getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDateTime getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDateTime expiryDate) {
    this.expiryDate = expiryDate;
  }

  public boolean isExpired() {
    return expiryDate.isBefore(LocalDateTime.now());
  }
}
