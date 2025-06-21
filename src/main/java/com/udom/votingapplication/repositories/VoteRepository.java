package com.udom.votingapplication.repositories;

import com.udom.votingapplication.models.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByVoterIdAndElectionId(Long voterId, Long electionId);
    long countByCandidateId(Long candidateId);
}
