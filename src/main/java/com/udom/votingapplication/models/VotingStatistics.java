package com.udom.votingapplication.models;

public class VotingStatistics {
    private long totalVotesCast;
    private long electionsParticipated;
    private double participationRate;
    
    public VotingStatistics() {}
    
    public VotingStatistics(long totalVotesCast, long electionsParticipated, double participationRate) {
        this.totalVotesCast = totalVotesCast;
        this.electionsParticipated = electionsParticipated;
        this.participationRate = participationRate;
    }
    
    public long getTotalVotesCast() {
        return totalVotesCast;
    }
    
    public void setTotalVotesCast(long totalVotesCast) {
        this.totalVotesCast = totalVotesCast;
    }
    
    public long getElectionsParticipated() {
        return electionsParticipated;
    }
    
    public void setElectionsParticipated(long electionsParticipated) {
        this.electionsParticipated = electionsParticipated;
    }
    
    public double getParticipationRate() {
        return participationRate;
    }
    
    public void setParticipationRate(double participationRate) {
        this.participationRate = participationRate;
    }
    
    public String getParticipationRatePercentage() {
        return String.format("%.0f%%", participationRate);
    }
}
