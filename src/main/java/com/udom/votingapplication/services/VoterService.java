package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Voter;
import com.udom.votingapplication.repositories.VoterRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class VoterService implements UserDetailsService {

    private final VoterRepository repo;
    private final PasswordEncoder encoder;

    public VoterService(VoterRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Voter register(Voter voter) {
        voter.setPassword(encoder.encode(voter.getPassword()));
        return repo.save(voter);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
