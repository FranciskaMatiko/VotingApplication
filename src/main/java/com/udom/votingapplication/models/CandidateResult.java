package com.udom.votingapplication.models;

public class CandidateResult {
    private Long candidateId;
    private String candidateName;
    private String party;
    private String position;
    private long voteCount;
    private double percentage;
    private boolean isWinner;

    public CandidateResult() {}

    public CandidateResult(Long candidateId, String candidateName, String party, 
                          String position, long voteCount, double percentage) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.party = party;
        this.position = position;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }

    // Getters and setters
    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public long getVoteCount() { return voteCount; }
    public void setVoteCount(long voteCount) { this.voteCount = voteCount; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public boolean isWinner() { return isWinner; }
    public void setWinner(boolean winner) { isWinner = winner; }
}
