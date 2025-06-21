package com.udom.votingapplication.services;

import com.udom.votingapplication.models.Election;
import com.udom.votingapplication.repositories.ElectionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ElectionService {
    private final ElectionRepository electionRepository;

    public ElectionService(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    public List<Election> getAllElections() {
        return electionRepository.findAll();
    }

    public Optional<Election> getElection(Long id) {
        return electionRepository.findById(id);
    }

    public Election saveElection(Election election) {
        return electionRepository.save(election);
    }

    public void deleteElection(Long id) {
        electionRepository.deleteById(id);
    }

    public List<Election> getActiveElections() {
        return electionRepository.findAll().stream()
            .filter(election -> election.getEndTime().isAfter(LocalDateTime.now()))
            .collect(Collectors.toList());
    }

    public List<Election> getCompletedElections() {
        return electionRepository.findAll().stream()
            .filter(election -> election.getEndTime().isBefore(LocalDateTime.now()))
            .collect(Collectors.toList());
    }

    public boolean isElectionActive(Election election) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(election.getStartTime()) && now.isBefore(election.getEndTime());
    }
}
