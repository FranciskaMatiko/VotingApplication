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

    public boolean changePassword(Long voterId, String currentPassword, String newPassword) {
        Optional<Voter> voterOpt = repo.findById(voterId);
        if (voterOpt.isEmpty()) {
            return false;
        }
        
        Voter voter = voterOpt.get();
        
        // Verify current password
        if (!encoder.matches(currentPassword, voter.getPassword())) {
            return false;
        }
        
        // Set new password
        voter.setPassword(encoder.encode(newPassword));
        repo.save(voter);
        return true;
    }
    
    public boolean updateProfile(Long voterId, String fullName, String email) {
        Optional<Voter> voterOpt = repo.findById(voterId);
        if (voterOpt.isEmpty()) {
            return false;
        }
        
        Voter voter = voterOpt.get();
        voter.setFullName(fullName);
        // Note: Email update might need additional validation in a real app
        // You might want to send a verification email before updating
        repo.save(voter);
        return true;
    }
    
    public boolean updateNotificationSettings(Long voterId, boolean electionNotifications, 
                                            boolean voteReminders, boolean resultNotifications, 
                                            boolean emailNotifications) {
        Optional<Voter> voterOpt = repo.findById(voterId);
        if (voterOpt.isEmpty()) {
            return false;
        }
        
        Voter voter = voterOpt.get();
        // In a real application, you would have notification preference fields in the Voter model
        // For now, we'll just log the settings (you can extend the Voter model later)
        System.out.println("Notification settings updated for voter " + voter.getUsername() + ":");
        System.out.println("Election notifications: " + electionNotifications);
        System.out.println("Vote reminders: " + voteReminders);
        System.out.println("Result notifications: " + resultNotifications);
        System.out.println("Email notifications: " + emailNotifications);
        
        // TODO: Save these preferences to database when you extend the Voter model
        // voter.setElectionNotifications(electionNotifications);
        // voter.setVoteReminders(voteReminders);
        // voter.setResultNotifications(resultNotifications);
        // voter.setEmailNotifications(emailNotifications);
        // repo.save(voter);
        
        return true;
    }
}
