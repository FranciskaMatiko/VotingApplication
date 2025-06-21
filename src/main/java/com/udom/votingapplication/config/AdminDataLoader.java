package com.udom.votingapplication.config;

import com.udom.votingapplication.models.Admin;
import com.udom.votingapplication.services.AdminService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data loader to create default admin account on application startup
 * This is a secure way to ensure there's always an admin account available
 */
@Component
public class AdminDataLoader implements CommandLineRunner {
    
    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;
    
    public AdminDataLoader(AdminService adminService, PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminIfNotExists();
    }
    
    private void createDefaultAdminIfNotExists() {
        try {
            // Check if any admin exists
            if (adminService.findByUsername("admin") == null) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("System Administrator");
                admin.setEmail("admin@voting.system");
                admin.setEnabled(true);
                
                adminService.save(admin);
                System.out.println("✅ Default admin account created successfully!");
                System.out.println("🔑 Username: admin");
                System.out.println("🔑 Password: admin123");
                System.out.println("⚠️  IMPORTANT: Change the default password after first login!");
            } else {
                System.out.println("ℹ️  Admin account already exists. Skipping creation.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating default admin account: " + e.getMessage());
        }
    }
}
