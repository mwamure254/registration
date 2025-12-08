package com.mfano.registration.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;

import com.mfano.registration.security.config.CustomUserDetails;
import com.mfano.registration.security.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {
  @Autowired
  private final RoleRepository roleRepo;

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

    // Add user info to model (for Thymeleaf dashboard pages)
    model.addAttribute("id", u.getId());
    model.addAttribute("username", u.getUsername());
    model.addAttribute("email", u.getEmail());
    model.addAttribute("firstname", u.getFin());
    model.addAttribute("lastname", u.getLan());
    model.addAttribute("roles", u.getRoles());

    // Extract roles
    Set<String> roles = u.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

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

    // Fallback if no roles
    return "redirect:/login?error=no_role";
  }

  // Individual dashboard pages
  @GetMapping("/admin/dashboard")
  public String adminDashboard(Model model) {
    model.addAttribute("roles", roleRepo.findAll());
    return "dashboards/admin";
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
