package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Vote;
import com.udom.votingapplication.models.Voter;
import com.udom.votingapplication.models.Candidate;
import com.udom.votingapplication.models.Election;
import com.udom.votingapplication.repositories.VoteRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoteService {
    private final VoteRepository voteRepository;

    public VoteService(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public boolean hasVoted(Long voterId, Long electionId) {
        return voteRepository.findByVoterIdAndElectionId(voterId, electionId).isPresent();
    }

    public Vote castVote(Voter voter, Candidate candidate, Election election) {
        if (hasVoted(voter.getId(), election.getId())) {
            throw new IllegalStateException("Voter has already voted in this election");
        }
        Vote vote = new Vote();
        vote.setVoter(voter);
        vote.setCandidate(candidate);
        vote.setElection(election);
        vote.setTimestamp(LocalDateTime.now());
        return voteRepository.save(vote);
    }

    public long countVotesForCandidate(Long candidateId) {
        return voteRepository.countByCandidateId(candidateId);
    }
    
    public long countVotesForElection(Long electionId) {
        return voteRepository.countByElectionId(electionId);
    }
    
    public List<Vote> getVotesForElection(Long electionId) {
        return voteRepository.findByElectionId(electionId);
    }
    
    public List<Object[]> getVoteCountsByElection(Long electionId) {
        return voteRepository.getVoteCountsByElection(electionId);
    }
}
