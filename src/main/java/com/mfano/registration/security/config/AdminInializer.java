package com.mfano.registration.security.config;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.mfano.registration.security.model.Role;
import com.mfano.registration.security.model.User;
import com.mfano.registration.security.repository.RoleRepository;
import com.mfano.registration.security.repository.UserRepository;

@Component
public class AdminInializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    Model model;
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        List<String> defaultRoles = List.of("ADMIN","DIRECTOR","HOI","USER");
        
        for (String r : defaultRoles) {
            if (roleRepo.findByName(r).isEmpty()) {
                Role role = new Role();
                role.setName(r);
                roleRepo.save(role);
                
            }
        }

        if (userRepository.findByEmail(adminEmail) == null) {
            User admin = new User();
            
            admin.setEmail(adminEmail);
            admin.setUsername(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEnabled(true);
            admin.setRoles(Set.of(roleRepo.findByName("ADMIN").get()));
            
            userRepository.save(admin);
        } 
    }

}
