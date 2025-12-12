package com.mfano.registration.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;

import com.mfano.registration.security.config.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

  @GetMapping("/dashboard")
  public String redirectAfterLogin(Authentication auth, Model model) {

    if (auth == null || !auth.isAuthenticated()) {
      model.addAttribute("error", "user not authenticated");
      return "redirect:/login";
    }

    Object principal = auth.getPrincipal();
    if (!(principal instanceof CustomUserDetails u)) {
      model.addAttribute("error", "invalid user");
      return "redirect:/login";
    }

    // Check if user is enabled
    if (!u.isEnabled()) {
      model.addAttribute("error", "user not verified");
      return "redirect:/login?error=not_verified";
    }

    // Extract roles
    Set<String> roles = u.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    // If user is not assigned any role
    if (roles.isEmpty()) {
      model.addAttribute("error", "contact the administrater");
      return "redirect:/login?error";
    }

    // Redirect based on role priority
    if (roles.contains("ROLE_ADMIN")) {
      return "redirect:/admin/dashboard";
    } else if (roles.contains("ROLE_DIRECTOR")) {
      return "redirect:/director/dashboard";
    } else if (roles.contains("ROLE_HOI")) {
      return "redirect:/hoi/dashboard";
    } else if (roles.contains("ROLE_USER")) {
      return "redirect:/user/dashboard";
    }

    // Fallback
    return "redirect:/login?error";
  }

  @GetMapping("/director/dashboard")
  public String directorDashboard() {
    return "dashboards/director";
  }

  @GetMapping("/hoi/dashboard")
  public String hoiDashboard() {
    return "dashboards/hoi";
  }

  @GetMapping("/user/dashboard")
  public String userDashboard() {
    return "dashboards/user";
  }
}
