package com.mfano.registration.security.controller;

import com.mfano.registration.security.model.User;
import com.mfano.registration.security.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/redirect";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute UserDto userDto, Model model) {
        try {
            userService.registerUser(userDto.getEmail(), userDto.getPassword(), userDto.getFullName(),
                    userDto.getRole());
            model.addAttribute("message", "Registration successful. Check your email for verification link.");
            return "message";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null)
            model.addAttribute("error", "Invalid credentials or email not verified.");
        if (logout != null)
            model.addAttribute("message", "You have been logged out.");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String userProfile() {
       
        return "profile";
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
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
        var opt = userService.findByEmail(email);
        if (opt.isEmpty()) {
            model.addAttribute("error", "No account with that email.");
            return "resend";
        }
        User user = opt.get();
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
