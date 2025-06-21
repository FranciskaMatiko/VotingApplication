package com.udom.votingapplication.services;

import com.udom.votingapplication.models.*;
import com.udom.votingapplication.repositories.CandidateRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResultService {
    private final VoteService voteService;
    private final ElectionService electionService;
    private final CandidateService candidateService;
    private final VoterService voterService;

    public ResultService(VoteService voteService, ElectionService electionService, 
                        CandidateService candidateService, VoterService voterService) {
        this.voteService = voteService;
        this.electionService = electionService;
        this.candidateService = candidateService;
        this.voterService = voterService;
    }

    public List<ElectionResult> getAllElectionResults() {
        List<Election> elections = electionService.getAllElections();
        List<ElectionResult> results = new ArrayList<>();
        
        for (Election election : elections) {
            ElectionResult result = getElectionResult(election);
            results.add(result);
        }
        
        return results;
    }

    public ElectionResult getElectionResult(Election election) {
        // Ensure status is calculated
        election.calculateStatus();
        
        long totalVotes = voteService.countVotesForElection(election.getId());
        long totalCandidates = election.getCandidates() != null ? election.getCandidates().size() : 0;
        long totalEligibleVoters = voterService.getAllVoters().size();
        
        ElectionResult result = new ElectionResult(
            election.getId(),
            election.getName(),
            election.getStatus(),
            totalVotes,
            totalCandidates,
            totalEligibleVoters
        );
        
        // Determine winner if election is completed and has votes
        if ("completed".equals(election.getStatus()) && totalVotes > 0) {
            List<CandidateResult> candidateResults = getCandidateResults(election.getId());
            if (!candidateResults.isEmpty()) {
                CandidateResult winner = candidateResults.stream()
                    .max(Comparator.comparing(CandidateResult::getVoteCount))
                    .orElse(null);
                if (winner != null) {
                    Candidate winnerCandidate = candidateService.getCandidate(winner.getCandidateId())
                        .orElse(null);
                    result.setWinner(winnerCandidate);
                }
            }
        }
        
        return result;
    }

    public List<CandidateResult> getCandidateResults(Long electionId) {
        Election election = electionService.getElection(electionId).orElse(null);
        if (election == null) {
            return new ArrayList<>();
        }

        List<Candidate> candidates = candidateService.getCandidatesByElection(election);
        long totalVotes = voteService.countVotesForElection(electionId);
        
        List<CandidateResult> results = new ArrayList<>();
        long maxVotes = 0;
        
        for (Candidate candidate : candidates) {
            long voteCount = voteService.countVotesForCandidate(candidate.getId());
            double percentage = totalVotes > 0 ? (double) voteCount / totalVotes * 100 : 0;
            
            CandidateResult result = new CandidateResult(
                candidate.getId(),
                candidate.getName(),
                candidate.getParty(),
                candidate.getPosition(),
                voteCount,
                percentage
            );
            
            results.add(result);
            if (voteCount > maxVotes) {
                maxVotes = voteCount;
            }
        }
        
        // Mark winners (candidates with highest votes)
        final long finalMaxVotes = maxVotes;
        if (maxVotes > 0) {
            results.forEach(result -> {
                if (result.getVoteCount() == finalMaxVotes) {
                    result.setWinner(true);
                }
            });
        }
        
        // Sort by vote count (descending)
        results.sort(Comparator.comparing(CandidateResult::getVoteCount).reversed());
        
        return results;
    }
}
