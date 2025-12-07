package com.mfano.registration.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mfano.registration.security.config.CustomUserDetails;
import com.mfano.registration.security.config.SecurityUtils;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;

@Controller
public class DashboardController {

  @GetMapping("/director/dashboard")
  public String directorDashboard(Model model) {
    return "dashboards/director";
  }

  @GetMapping("/hoi/dashboard")
  public String hoiDashboard(Model model) {
    return "dashboards/hoi";
  }

  @GetMapping("/user/dashboard")
  public String userDashboard(Model model) {
    return "dashboards/user";
  }

  @GetMapping("/dashboard")
  public String redirectAfterLogin(Authentication authentication, Model model) {

    CustomUserDetails u = SecurityUtils.getCurrentUser();
    if (u != null) {
      model.addAttribute("Email", u.getEmail());
      model.addAttribute("Username", u.getUsername());
      model.addAttribute("Firstname", u.getFin());
      model.addAttribute("Lastname", u.getLan());
      model.addAttribute("Id", u.getId());
      model.addAttribute("Enabled", u.isEnabled());
      model.addAttribute("Roles", u.getRoles());

      if (!u.isEnabled()) {
        model.addAttribute("error","User not verified");
      }

      // Extract all roles of the logged-in user
      Set<String> roles = authentication.getAuthorities()
          .stream()
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toSet());

      // Redirect based on role priority
      if (roles.contains("ROLE_ADMIN")) {
        return "redirect:/admin/dashboard";
      }

      if (roles.contains("ROLE_DIRECTOR")) {
        return "redirect:/director/dashboard";
      }

      if (roles.contains("ROLE_HOI")) {
        return "redirect:/hoi/dashboard";
      }
    }

    return "redirect:/user/dashboard";
  }

}
