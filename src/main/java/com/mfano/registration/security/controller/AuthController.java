package com.mfano.registration.security.controller;

import com.mfano.registration.security.config.CustomUserDetails;
import com.mfano.registration.security.config.UserDto;
import com.mfano.registration.security.model.User;
import com.mfano.registration.security.repository.RoleRepository;
import com.mfano.registration.security.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private String msg = "security/message";
    private final String login = "redirect:/login?error";

    @GetMapping("/dashboard")
    public String redirectAfterLogin(Authentication auth, Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            model.addAttribute("error", "user not authenticated");
            return login;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails u)) {
            model.addAttribute("error", "invalid user");
            return login;
        }

        // Check if user is enabled
        if (!u.isEnabled()) {
            model.addAttribute("error", "user not verified");
            return login;
        }

        // Extract roles
        Set<String> roles = u.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // If user is not assigned any role
        if (roles.isEmpty()) {
            model.addAttribute("error", "contact the system admin");
            return login;
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
        model.addAttribute("error", "Please contact the system admin");
        return login;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {

        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("userDto", new UserDto());
        return "security/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute UserDto userDto, Model model) {
        try {
            userService.registerUser(userDto.getEmail(), userDto.getPassword(), userDto.getRoles());
            model.addAttribute("message", "Registration successful. Check your email for verification link.");
            return msg;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "security/register";
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

        return "security/login"; // Return login view
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String userProfile(Authentication auth, Model model) {
        if (!(auth.getPrincipal() instanceof CustomUserDetails u)) {
            model.addAttribute("error", "user not authenticated");
            return "redirect:/login";
        }
        // Add user info to model (for Thymeleaf dashboard pages)
        model.addAttribute("id", u.getId());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("email", u.getEmail());
        model.addAttribute("firstname", u.getFin());
        model.addAttribute("lastname", u.getLan());
        model.addAttribute("roles", u.getRoles());

        return "security/profile";
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        model.addAttribute("message", "You have been logged out successfully");
        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String verify(@RequestParam("token") String token, Model model) {
        String result = userService.validateVerificationToken(token);
        if ("valid".equals(result)) {
            model.addAttribute("message", "Email verified! You can now login.");
            return msg;
        } else if ("expired".equals(result)) {
            model.addAttribute("error", "Token expired. Please register again.");
            return msg;
        } else {
            model.addAttribute("error", "Invalid token.");
            return msg;
        }
    }

    @GetMapping("/resend")
    public String resendForm() {
        return "security/resend";
    }

    @PostMapping("/resend")
    public String resendSubmit(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "No account with that email.");
            return "security/resend";
        }

        if (user.isEnabled()) {
            model.addAttribute("message", "Email already verified. You can login.");
            return msg;
        }
        userService.createAndSendToken(user);
        model.addAttribute("message", "Verification email resent. Check your inbox.");
        return msg;
    }

    // Forgot/reset endpoints
    @GetMapping("/forgot")
    public String forgotForm() {
        return "security/forgot";
    }

    @PostMapping("/forgot")
    public String forgotSubmit(@RequestParam String email, Model model) {
        try {
            userService.createPasswordResetToken(email);
            model.addAttribute("message", "If an account exists, a reset link was sent.");
        } catch (Exception e) {
            model.addAttribute("message", "If an account exists, a reset link was sent.");
        }
        return msg;
    }

    @GetMapping("/password-reset")
    public String resetPasswordForm(@RequestParam("token") String token, Model model) {
        String res = userService.validatePasswordResetToken(token);
        if ("valid".equals(res)) {
            model.addAttribute("token", token);
            return "security/reset-password";
        } else if ("expired".equals(res)) {
            model.addAttribute("error", "Token expired.");
            return msg;
        } else {
            model.addAttribute("error", "Invalid token.");
            return msg;
        }
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token, @RequestParam String password, Model model) {
        var optUser = userService.getUserByPasswordResetToken(token);
        if (optUser.isEmpty()) {
            model.addAttribute("error", "Invalid token.");
            return msg;
        }
        userService.changePassword(optUser.get(), password);
        model.addAttribute("message", "Password changed. You can now login.");
        return msg;
    }
}
