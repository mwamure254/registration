package com.mfano.registration.security.controller;

import com.mfano.registration.security.model.Role;
import com.mfano.registration.security.model.User;
import com.mfano.registration.security.repository.UserRepository;
import com.mfano.registration.security.service.UserService;
import com.mfano.registration.security.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userRepository.findAll();
        
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        model.addAttribute("auditEntries", auditService.findAll());
        return "dashboards/admin";
    }

    // Create a new user
    @PostMapping("/create")
    public String createUser(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String role,
                             Model model) {
        try {
            userService.registerUser(email, password, fullName, role);
            model.addAttribute("message", "User created and verification email sent.");
            auditService.record("CREATE_USER", "admin", "Created user: " + email);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // Enable/Disable user
    @GetMapping("/toggle/{id}")
    public String toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            auditService.record("TOGGLE_USER", "admin", "Toggled user: " + user.getEmail() + " to enabled=" + user.isEnabled());
        }
        return "redirect:/admin/dashboard";
    }

    // Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        auditService.record("DELETE_USER", "admin", "Deleted user id=" + id);
        return "redirect:/admin/dashboard";
    }

    // Change role
    @PostMapping("/update-role/{id}")
    public String updateRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(Role.valueOf(role.toUpperCase()));
            userRepository.save(user);
            auditService.record("UPDATE_ROLE", "admin", "Updated role for user id=" + id + " to " + role);
        }
        return "redirect:/admin/dashboard";
    }

    // Reset user password
    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            auditService.record("RESET_PASSWORD", "admin", "Reset password for user id=" + id);
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/resend/{id}")
    public String adminResend(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && !user.isEnabled()) {
            userService.createAndSendToken(user);
            auditService.record("RESEND_VERIFICATION", "admin", "Resent token to user id=" + id);
        }
        return "redirect:/admin/dashboard";
    }
}
