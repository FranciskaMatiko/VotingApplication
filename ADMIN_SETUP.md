# Admin Account Setup

## Security Notice
For security reasons, admin registration has been disabled. Admin accounts can only be created manually by a system administrator or through a future super admin panel.

## Creating an Admin Account

### Method 1: SQL Database Insert (Recommended)

1. Start your MySQL/MariaDB server
2. Connect to your database
3. Use the following SQL to create an admin account:

```sql
-- Create admin account with username 'admin' and password 'admin123'
-- Password is BCrypt hashed for 'admin123'
INSERT INTO admin (username, password, full_name, email, enabled) 
VALUES ('admin', '$2a$10$Q2WMsE8P6oXIJjm2FGKN9.cBmVGWZqV9L6Bz8xrFwQq5mNt7Js6i.', 'System Administrator', 'admin@voting.system', true);
```

### Method 2: Application Data Loader

Create a data loader in your Spring Boot application:

```java
@Component
public class AdminDataLoader implements CommandLineRunner {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if admin exists
        if (adminService.findByUsername("admin") == null) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setEmail("admin@voting.system");
            admin.setEnabled(true);
            
            adminService.save(admin);
            System.out.println("Default admin account created: admin/admin123");
        }
    }
}
```

## Default Admin Credentials

**Username:** admin  
**Password:** admin123

> ⚠️ **IMPORTANT:** Change the default password immediately after first login!

## Future Enhancement

Consider implementing a super admin panel that allows:
- Creating new admin accounts
- Managing admin privileges
- Audit logging for admin activities
- Password reset functionality

## Security Best Practices

1. Always use strong passwords for admin accounts
2. Consider implementing 2FA for admin accounts
3. Regularly audit admin access logs
4. Limit the number of admin accounts
5. Use role-based access control for different admin levels
