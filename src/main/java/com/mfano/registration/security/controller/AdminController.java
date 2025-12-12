package com.mfano.registration.security.controller;

import com.mfano.registration.security.config.UserDto;
import com.mfano.registration.security.model.User;
import com.mfano.registration.security.repository.RoleRepository;
import com.mfano.registration.security.repository.UserRepository;
import com.mfano.registration.security.service.UserService;
import com.mfano.registration.security.service.AdminService;
import com.mfano.registration.security.service.AuditService;
import com.mfano.registration.security.service.RoleService;

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
    private final RoleRepository roles;
    private final RoleService roleService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userRepository.findAll();

        model.addAttribute("userDto", new UserDto());
        model.addAttribute("users", users);
        model.addAttribute("roles", roles.findAll());
        model.addAttribute("auditEntries", auditService.findAll());
        return "admin/dashboard";
    }

    private String redirect = "redirect:/admin/dashboard";

    // Create a new user
    @PostMapping("/create")
    public String createUser(@ModelAttribute UserDto userDto,
            Model model) {
        try {
            userService.registerUser(userDto.getEmail(), userDto.getPassword(), userDto.getRoles());
            model.addAttribute("message", "User created and verification email sent.");
            auditService.record("CREATE_USER", "admin", "Created user: " + userDto.getEmail());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return redirect;
    }

    // Enable/Disable user
    @GetMapping("/toggle/{id}")
    public String toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            auditService.record("TOGGLE_USER", "admin",
                    "Toggled user: " + user.getEmail() + " to enabled=" + user.isEnabled());
        }
        return redirect;
    }

    // Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, Model model) {
        try {
            userRepository.deleteById(id);
            auditService.record("DELETE_USER", "admin", "Deleted user id=" + id);
            model.addAttribute("message", "user successfully deleted");
            return redirect;
        } catch (Exception e) {
            model.addAttribute("error", "Sorry! Failed to delete user");
            return redirect;
        }
    }

    // Change role
    @PostMapping("/update-role/{id}")
    public String updateRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            userRepository.save(user);
            auditService.record("UPDATE_ROLE", "admin", "Updated role for user id=" + id + " to " + role);
        }
        return redirect;
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
        return redirect;
    }

    @GetMapping("/resend/{id}")
    public String adminResend(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && !user.isEnabled()) {
            userService.createAndSendToken(user);
            auditService.record("RESEND_VERIFICATION", "admin", "Resent token to user id=" + id);
        }
        return redirect;
    }

    @GetMapping("/manage-roles/{id}")
    public String manageRoles(@PathVariable Long id, Model model) {
        User user = userService.findById(id);

        model.addAttribute("user", user);
        model.addAttribute("userRoles", roleService.getUserRoles(user));
        model.addAttribute("userNotRoles", roleService.getUserNotRoles(user));

        return "admin/manage-roles";
    }

    @PostMapping("/assign-role/{userId}/{roleId}")
    public String assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        adminService.assignRoleToUser(userId, roleId);
        return "redirect:/admin/manage-roles/{userId}";
    }

    @PostMapping("/remove-role/{userId}/{roleId}")
    public String removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        adminService.removeRoleFromUser(userId, roleId);
        return "redirect:/admin/manage-roles/{userId}";
    }

}
