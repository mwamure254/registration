package com.mfano.registration.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mfano.registration.security.model.CustomUserDetails;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

@Controller
public class DashboardController {
  /*
   * @GetMapping("/admin/dashboard")
   * public String adminDashboard(Model model) {
   * return "dashboards/admin";
   * }
   */
  @GetMapping("/director/dashboard")
  public String directorDashboard(Model model) {
    return "dashboards/director";
  }

  @GetMapping("/hoi/dashboard")
  public String hoiDashboard(Model model) {
    return "dashboards/hoi";
  }

  @GetMapping("/user/dashboard")
  public String userDashboard(@AuthenticationPrincipal UserDetails user, Model model) {
    model.addAttribute("email", user.getUsername());
    return "dashboards/user";
  }

  @GetMapping("/redirect")
  public String redirectAfterLogin(org.springframework.security.core.Authentication authentication,
      Model model) {
    String role = authentication.getAuthorities().iterator().next().getAuthority();

    Object principal = authentication.getPrincipal();
    if (principal instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) principal;
      model.addAttribute("user", userDetails.getUser());
    }

    if (role.equals("ROLE_ADMIN"))
      return "redirect:/admin/dashboard";
    if (role.equals("ROLE_DIRECTOR"))
      return "redirect:/director/dashboard";
    if (role.equals("ROLE_HOI"))
      return "redirect:/hoi/dashboard";
    return "redirect:/user/dashboard";
  }

}
