package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Candidate;
import com.udom.votingapplication.models.Election;
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

    public List<Candidate> getCandidatesByElection(Election election) {
        return candidateRepository.findByElectionId(election.getId());
    }

    public Optional<Candidate> getCandidate(Long id) {
        return candidateRepository.findById(id);
    }

    public Candidate saveCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }
}
