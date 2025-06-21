package com.udom.votingapplication.controllers;

import com.udom.votingapplication.models.*;
import com.udom.votingapplication.services.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

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
        // Add dashboard statistics
        List<Election> allElections = electionService.getAllElections();
        long activeElections = allElections.stream()
            .filter(e -> e.getEndTime().isAfter(LocalDateTime.now()))
            .count();
        
        model.addAttribute("activeElections", activeElections);
        model.addAttribute("totalElections", allElections.size());
        
        return "voter";
    }

    // View all elections
    @GetMapping("/elections")
    public String elections(Model model, @AuthenticationPrincipal Voter voter) {
        List<Election> elections = electionService.getAllElections();
        
        // Add voting status for each election
        for (Election election : elections) {
            boolean hasVoted = voteService.hasVoted(voter.getId(), election.getId());
            election.setVoterHasVoted(hasVoted);
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
        if (voteService.hasVoted(voter.getId(), electionId)) {
            return "redirect:/voter/confirmation?alreadyVoted";
        }
        List<Candidate> candidates = candidateService.getCandidatesByElection(electionId);
        model.addAttribute("candidates", candidates);
        model.addAttribute("electionId", electionId);
        return "vote";
    }

    @PostMapping("/elections/{electionId}/vote")
    public String castVote(@PathVariable Long electionId, @RequestParam Long candidateId, @AuthenticationPrincipal Voter voter, Model model) {
        if (voteService.hasVoted(voter.getId(), electionId)) {
            return "redirect:/voter/confirmation?alreadyVoted";
        }
        Candidate candidate = candidateService.getCandidate(candidateId).orElseThrow();
        Election election = electionService.getElection(electionId).orElseThrow();
        voteService.castVote(voter, candidate, election);
        return "redirect:/voter/confirmation?success";
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
}
