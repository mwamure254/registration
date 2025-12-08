package com.mfano.registration.security.controller;

import com.mfano.registration.security.config.CustomUserDetails;
import com.mfano.registration.security.config.UserDto;
import com.mfano.registration.security.model.User;
import com.mfano.registration.security.repository.RoleRepository;
import com.mfano.registration.security.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final RoleRepository roleRepo;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {

        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute UserDto userDto, Model model) {
        try {
            userService.registerUser(userDto.getEmail(), userDto.getPassword(), userDto.getRoles());
            model.addAttribute("message", "Registration successful. Check your email for verification link.");
            return "message";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            Authentication authentication) {

        // If user is already logged in → redirect to dashboard
        if (authentication != null && authentication.isAuthenticated()
                && authentication instanceof CustomUserDetails) {
            return "redirect:/dashboard";
        }

        // Error from Spring Security (bad credentials or disabled)
        if (error != null) {
            model.addAttribute("error", "Invalid Username or Password.");
        }

        // Logout confirmation
        if (logout != null) {
            model.addAttribute("message", "You have been logged out.");
        }

        return "login"; // Return login view
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String userProfile() {

        return "profile";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String verify(@RequestParam("token") String token, Model model) {
        String result = userService.validateVerificationToken(token);
        if ("valid".equals(result)) {
            model.addAttribute("message", "Email verified! You can now login.");
            return "message";
        } else if ("expired".equals(result)) {
            model.addAttribute("error", "Token expired. Please register again.");
            return "message";
        } else {
            model.addAttribute("error", "Invalid token.");
            return "message";
        }
    }

    @GetMapping("/resend")
    public String resendForm() {
        return "resend";
    }

    @PostMapping("/resend")
    public String resendSubmit(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "No account with that email.");
            return "resend";
        }

        if (user.isEnabled()) {
            model.addAttribute("message", "Email already verified. You can login.");
            return "message";
        }
        userService.createAndSendToken(user);
        model.addAttribute("message", "Verification email resent. Check your inbox.");
        return "message";
    }

    // Forgot/reset endpoints
    @GetMapping("/forgot")
    public String forgotForm() {
        return "forgot";
    }

    @PostMapping("/forgot")
    public String forgotSubmit(@RequestParam String email, Model model) {
        try {
            userService.createPasswordResetToken(email);
            model.addAttribute("message", "If an account exists, a reset link was sent.");
        } catch (Exception e) {
            model.addAttribute("message", "If an account exists, a reset link was sent.");
        }
        return "message";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, Model model) {
        String res = userService.validatePasswordResetToken(token);
        if ("valid".equals(res)) {
            model.addAttribute("token", token);
            return "reset-password";
        } else if ("expired".equals(res)) {
            model.addAttribute("error", "Token expired.");
            return "message";
        } else {
            model.addAttribute("error", "Invalid token.");
            return "message";
        }
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token, @RequestParam String password, Model model) {
        var optUser = userService.getUserByPasswordResetToken(token);
        if (optUser.isEmpty()) {
            model.addAttribute("error", "Invalid token.");
            return "message";
        }
        userService.changePassword(optUser.get(), password);
        model.addAttribute("message", "Password changed. You can now login.");
        return "message";
    }
}
