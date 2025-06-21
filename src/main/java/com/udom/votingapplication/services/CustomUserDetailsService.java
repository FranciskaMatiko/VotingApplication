package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Admin;
import com.udom.votingapplication.models.Voter;
import com.udom.votingapplication.repositories.AdminRepository;
import com.udom.votingapplication.repositories.VoterRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final VoterRepository voterRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(VoterRepository voterRepository, AdminRepository adminRepository) {
        this.voterRepository = voterRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First try to find admin
        return adminRepository.findByUsername(username)
                .map(admin -> (UserDetails) admin)
                .orElseGet(() -> 
                    voterRepository.findByUsername(username)
                        .map(voter -> (UserDetails) voter)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"))
                );
    }
}
