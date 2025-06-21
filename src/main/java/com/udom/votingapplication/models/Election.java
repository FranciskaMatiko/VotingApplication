package com.udom.votingapplication.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Election {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean resultsVisible;
    private String votingType = "single"; // single or multiple

    @OneToMany(mappedBy = "election")
    private List<Candidate> candidates;

    // Add transient field for UI purposes
    @Transient
    private boolean voterHasVoted;
    
    @Transient
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isResultsVisible() {
        return resultsVisible;
    }

    public void setResultsVisible(boolean resultsVisible) {
        this.resultsVisible = resultsVisible;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public boolean isVoterHasVoted() { return voterHasVoted; }
    public void setVoterHasVoted(boolean voterHasVoted) { this.voterHasVoted = voterHasVoted; }

    public String getVotingType() {
        return votingType;
    }

    public void setVotingType(String votingType) {
        this.votingType = votingType;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void calculateStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (endTime.isBefore(now)) {
            this.status = "completed";
        } else if (startTime.isAfter(now)) {
            this.status = "upcoming";
        } else {
            this.status = "active";
        }
    }
}
