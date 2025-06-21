package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Voter;
import com.udom.votingapplication.repositories.VoterRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

    // CRUD operations for admin management
    public List<Voter> getAllVoters() {
        return repo.findAll();
    }

    public Optional<Voter> getVoter(Long id) {
        return repo.findById(id);
    }

    public Voter saveVoter(Voter voter) {
        // Encode password if it's being set/changed
        if (voter.getPassword() != null && !voter.getPassword().isEmpty()) {
            voter.setPassword(encoder.encode(voter.getPassword()));
        }
        return repo.save(voter);
    }

    public void deleteVoter(Long id) {
        repo.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return repo.findByUsername(username).isPresent();
    }

    public List<Voter> searchVoters(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllVoters();
        }
        return repo.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query);
    }
}
