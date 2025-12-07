package com.mfano.registration.security.repository;

import com.mfano.registration.security.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  public User findByEmail(String email);

  public User findByUsername(String username);
}
