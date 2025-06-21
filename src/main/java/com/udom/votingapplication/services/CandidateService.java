package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Candidate;
import com.udom.votingapplication.repositories.CandidateRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public List<Candidate> getCandidatesByElection(Long electionId) {
        return candidateRepository.findByElectionId(electionId);
    }

    public Optional<Candidate> getCandidate(Long id) {
        return candidateRepository.findById(id);
    }
}
