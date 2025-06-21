package com.udom.votingapplication.controllers;

import com.udom.votingapplication.models.*;
import com.udom.votingapplication.services.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/voter")
public class VoterController {
    private final ElectionService electionService;
    private final CandidateService candidateService;
    private final VoteService voteService;

    public VoterController(ElectionService electionService, CandidateService candidateService, VoteService voteService) {
        this.electionService = electionService;
        this.candidateService = candidateService;
        this.voteService = voteService;
    }

    // Voter dashboard
    @GetMapping("")
    public String dashboard(Model model, @AuthenticationPrincipal Voter voter) {
        // Get all elections and calculate statistics
        List<Election> allElections = electionService.getAllElections();
        
        // Calculate status for each election
        for (Election election : allElections) {
            election.calculateStatus();
            boolean hasVoted = voteService.hasVoted(voter.getId(), election.getId());
            election.setVoterHasVoted(hasVoted);
        }
        
        // Count elections by status
        long activeElections = allElections.stream()
            .filter(e -> "active".equals(e.getStatus()))
            .count();
            
        long upcomingElections = allElections.stream()
            .filter(e -> "upcoming".equals(e.getStatus()))
            .count();
            
        long completedElections = allElections.stream()
            .filter(e -> "completed".equals(e.getStatus()))
            .count();
        
        // Count votes cast by this voter
        long votesCast = allElections.stream()
            .filter(e -> voteService.hasVoted(voter.getId(), e.getId()))
            .count();
            
        // Count pending votes (active elections where user hasn't voted)
        long pendingVotes = allElections.stream()
            .filter(e -> "active".equals(e.getStatus()) && !voteService.hasVoted(voter.getId(), e.getId()))
            .count();
            
        // Count results available (completed elections with visible results)
        long resultsAvailable = allElections.stream()
            .filter(e -> "completed".equals(e.getStatus()) && e.isResultsVisible())
            .count();
        
        // Get current active elections for dashboard display
        List<Election> currentElections = allElections.stream()
            .filter(e -> "active".equals(e.getStatus()) || "upcoming".equals(e.getStatus()))
            .limit(5) // Show max 5 elections on dashboard
            .collect(Collectors.toList());
        
        // Add all data to model
        model.addAttribute("activeElections", activeElections);
        model.addAttribute("upcomingElections", upcomingElections);
        model.addAttribute("completedElections", completedElections);
        model.addAttribute("totalElections", allElections.size());
        model.addAttribute("votesCast", votesCast);
        model.addAttribute("pendingVotes", pendingVotes);
        model.addAttribute("resultsAvailable", resultsAvailable);
        model.addAttribute("currentElections", currentElections);
        
        return "voter";
    }

    // View all elections
    @GetMapping("/elections")
    public String elections(Model model, @AuthenticationPrincipal Voter voter) {
        List<Election> elections = electionService.getAllElections();
        
        // Add voting status and status for each election
        for (Election election : elections) {
            boolean hasVoted = voteService.hasVoted(voter.getId(), election.getId());
            election.setVoterHasVoted(hasVoted);
            election.calculateStatus(); // Calculate status for UI
        }
        
        model.addAttribute("elections", elections);
        return "elections";
    }

    // View candidates for an election
    @GetMapping("/elections/{electionId}/candidates")
    public String candidates(@PathVariable Long electionId, Model model) {
        List<Candidate> candidates = candidateService.getCandidatesByElection(electionId);
        model.addAttribute("candidates", candidates);
        model.addAttribute("electionId", electionId);
        return "candidates";
    }

    // Cast vote
    @GetMapping("/elections/{electionId}/vote")
    public String voteForm(@PathVariable Long electionId, Model model, @AuthenticationPrincipal Voter voter) {
        Election election = electionService.getElection(electionId)
            .orElseThrow(() -> new RuntimeException("Election not found"));
        
        // Check if election is active
        election.calculateStatus();
        if (!"active".equals(election.getStatus())) {
            return "redirect:/voter/elections?error=notActive";
        }
        
        // Check if voter has already voted
        if (voteService.hasVoted(voter.getId(), electionId)) {
            return "redirect:/voter/confirmation?alreadyVoted";
        }
        
        List<Candidate> candidates = candidateService.getCandidatesByElection(electionId);
        
        model.addAttribute("election", election);
        model.addAttribute("candidates", candidates);
        model.addAttribute("electionId", electionId);
        return "vote";
    }

    @PostMapping("/elections/{electionId}/vote")
    public String castVote(@PathVariable Long electionId, @RequestParam Long candidateId, 
                          @AuthenticationPrincipal Voter voter, Model model) {
        try {
            Election election = electionService.getElection(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));
            
            // Check if election is still active
            election.calculateStatus();
            if (!"active".equals(election.getStatus())) {
                return "redirect:/voter/elections?error=notActive";
            }
            
            // Check if voter has already voted
            if (voteService.hasVoted(voter.getId(), electionId)) {
                return "redirect:/voter/confirmation?alreadyVoted";
            }
            
            Candidate candidate = candidateService.getCandidate(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
            
            // Verify candidate belongs to this election
            if (!candidate.getElection().getId().equals(electionId)) {
                return "redirect:/voter/elections?error=invalidCandidate";
            }
            
            voteService.castVote(voter, candidate, election);
            return "redirect:/voter/confirmation?success&election=" + electionId + "&candidate=" + candidateId;
            
        } catch (Exception e) {
            return "redirect:/voter/elections?error=votingFailed";
        }
    }

    // Vote confirmation
    @GetMapping("/confirmation")
    public String confirmation(@RequestParam(required = false) String alreadyVoted, @RequestParam(required = false) String success, Model model) {
        if (alreadyVoted != null) {
            model.addAttribute("message", "You have already voted in this election.");
        } else if (success != null) {
            model.addAttribute("message", "Your vote has been cast successfully.");
        }
        return "confirmation";
    }

    // View results
    @GetMapping("/elections/{electionId}/results")
    public String results(@PathVariable Long electionId, Model model) {
        Election election = electionService.getElection(electionId).orElseThrow();
        if (!election.isResultsVisible()) {
            model.addAttribute("message", "Results are not available yet.");
            return "results";
        }
        List<Candidate> candidates = candidateService.getCandidatesByElection(electionId);
        model.addAttribute("candidates", candidates);
        model.addAttribute("election", election);
        return "results";
    }

    // Voter profile page
    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal Voter voter) {
        model.addAttribute("voter", voter);
        return "voter-profile";
    }

    // General cast vote page (redirects to elections)
    @GetMapping("/vote")
    public String vote() {
        return "redirect:/voter/elections";
    }

    // Vote history page
    @GetMapping("/history")
    public String history(Model model, @AuthenticationPrincipal Voter voter) {
        List<Election> allElections = electionService.getAllElections();
        List<Election> votedElections = allElections.stream()
            .filter(election -> voteService.hasVoted(voter.getId(), election.getId()))
            .collect(Collectors.toList());
        
        for (Election election : votedElections) {
            election.calculateStatus();
        }
        
        model.addAttribute("votedElections", votedElections);
        return "voter-history";
    }

    // Help page
    @GetMapping("/help")
    public String help() {
        return "voter-help";
    }
}
