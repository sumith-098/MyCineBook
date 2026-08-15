package com.cinebook.auth.config;

import com.cinebook.auth.entity.Admin;
import com.cinebook.auth.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the default admin account on first boot, replacing the old seed_passwords.py script.
 * Default login: admin@cinebook.com / admin123 — CHANGE THIS PASSWORD after first login in production.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@cinebook.com}")
    private String seedAdminEmail;

    @Value("${app.seed.admin-password:admin123}")
    private String seedAdminPassword;

    public DataSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.findByEmail(seedAdminEmail).isEmpty()) {
            Admin admin = new Admin();
            admin.setName("Super Admin");
            admin.setEmail(seedAdminEmail);
            admin.setPasswordHash(passwordEncoder.encode(seedAdminPassword));
            adminRepository.save(admin);
            log.info("Seeded default admin account: {} (change the password after first login!)", seedAdminEmail);
        }
    }
}
