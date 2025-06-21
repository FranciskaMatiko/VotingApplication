package com.udom.votingapplication.models;

public class ElectionResult {
    private Long electionId;
    private String electionName;
    private String status;
    private long totalVotes;
    private long totalCandidates;
    private long totalEligibleVoters;
    private double participationRate;
    private Candidate winner;
    private boolean hasResults;

    public ElectionResult() {}

    public ElectionResult(Long electionId, String electionName, String status, 
                         long totalVotes, long totalCandidates, long totalEligibleVoters) {
        this.electionId = electionId;
        this.electionName = electionName;
        this.status = status;
        this.totalVotes = totalVotes;
        this.totalCandidates = totalCandidates;
        this.totalEligibleVoters = totalEligibleVoters;
        this.participationRate = totalEligibleVoters > 0 ? (double) totalVotes / totalEligibleVoters * 100 : 0;
        this.hasResults = totalVotes > 0;
    }

    // Getters and setters
    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public String getElectionName() { return electionName; }
    public void setElectionName(String electionName) { this.electionName = electionName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTotalVotes() { return totalVotes; }
    public void setTotalVotes(long totalVotes) { this.totalVotes = totalVotes; }

    public long getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(long totalCandidates) { this.totalCandidates = totalCandidates; }

    public long getTotalEligibleVoters() { return totalEligibleVoters; }
    public void setTotalEligibleVoters(long totalEligibleVoters) { this.totalEligibleVoters = totalEligibleVoters; }

    public double getParticipationRate() { return participationRate; }
    public void setParticipationRate(double participationRate) { this.participationRate = participationRate; }

    public Candidate getWinner() { return winner; }
    public void setWinner(Candidate winner) { this.winner = winner; }

    public boolean isHasResults() { return hasResults; }
    public void setHasResults(boolean hasResults) { this.hasResults = hasResults; }
}
