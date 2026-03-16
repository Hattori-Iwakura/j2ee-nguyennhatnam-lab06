package com.example.demo.config;

import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create roles if not exist
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));

        // Create default admin account if not exist
        if (!accountRepository.existsByUsername("admin")) {
            Account admin = new Account("admin", passwordEncoder.encode("admin123"), "admin@techshop.com", "Administrator");
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole);
            accountRepository.save(admin);
        }

        // Create default user account if not exist
        if (!accountRepository.existsByUsername("user")) {
            Account user = new Account("user", passwordEncoder.encode("user123"), "user@techshop.com", "Normal User");
            user.getRoles().add(userRole);
            accountRepository.save(user);
        }
    }
}
