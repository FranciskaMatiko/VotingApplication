package com.udom.votingapplication.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String profile;
    private String party;
    private String position;

    @ManyToOne
    @JoinColumn(name = "election_id")
    private Election election;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Election getElection() { return election; }
    public void setElection(Election election) { this.election = election; }
    public List<Vote> getVotes() { return votes; }
    public void setVotes(List<Vote> votes) { this.votes = votes; }
}
