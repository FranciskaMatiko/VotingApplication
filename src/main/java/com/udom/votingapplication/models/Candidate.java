package com.udom.votingapplication.models;

import jakarta.persistence.*;

@Entity
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String profile;

    @ManyToOne
    @JoinColumn(name = "election_id")
    private Election election;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public Election getElection() { return election; }
    public void setElection(Election election) { this.election = election; }
}
