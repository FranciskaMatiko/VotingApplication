package com.udom.votingapplication.repositories;

import com.udom.votingapplication.models.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByVoterIdAndElectionId(Long voterId, Long electionId);
    long countByCandidateId(Long candidateId);
    long countByElectionId(Long electionId);
    List<Vote> findByElectionId(Long electionId);
    
    @Query("SELECT v.candidate.id, COUNT(v) FROM Vote v WHERE v.election.id = :electionId GROUP BY v.candidate.id")
    List<Object[]> getVoteCountsByElection(@Param("electionId") Long electionId);
}
