package com.udom.votingapplication.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import com.udom.votingapplication.models.Voter;
import java.util.Optional;

public interface VoterRepository extends JpaRepository<Voter, Long> {
    Optional<Voter> findByUsername(String username);
}

