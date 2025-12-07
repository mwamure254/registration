package com.mfano.registration.security.service;

import com.mfano.registration.security.model.Role;
import com.mfano.registration.security.model.User;
import com.mfano.registration.security.repository.RoleRepository;
import com.mfano.registration.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public List<User> getAllUsers() { return userRepo.findAll(); }
    public List<Role> getAllRoles() { return roleRepo.findAll(); }

    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepo.findById(userId).orElseThrow();
        Role role = roleRepo.findById(roleId).orElseThrow();
        user.getRoles().add(role);
        userRepo.save(user);
    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepo.findById(userId).orElseThrow();
        Role role = roleRepo.findById(roleId).orElseThrow();
        user.getRoles().removeIf(r->r.getId().equals(role.getId()));
        userRepo.save(user);
    }
}
