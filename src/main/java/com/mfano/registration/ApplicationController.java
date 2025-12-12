package com.mfano.registration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ApplicationController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    // Error
    @GetMapping("/error-page")
    public String errorPage() {
        return "security/error";
    }

  @GetMapping("/director/dashboard")
  public String directorDashboard() {
    return "director/dashboard";
  }

  @GetMapping("/hoi/dashboard")
  public String hoiDashboard() {
    return "hoi/dashboard";
  }

  @GetMapping("/user/dashboard")
  public String userDashboard() {
    return "user/dashboard";
  }
}
