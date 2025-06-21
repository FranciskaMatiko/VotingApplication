package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Admin;
import com.udom.votingapplication.repositories.AdminRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder encoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder encoder) {
        this.adminRepository = adminRepository;
        this.encoder = encoder;
    }

    public Admin register(Admin admin) {
        admin.setPassword(encoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First try to find admin
        return adminRepository.findByUsername(username)
                .map(admin -> (UserDetails) admin)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
