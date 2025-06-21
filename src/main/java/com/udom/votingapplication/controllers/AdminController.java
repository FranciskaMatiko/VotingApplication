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
@RequestMapping("/admin")
public class AdminController {
    
    private final ElectionService electionService;
    private final CandidateService candidateService;
    private final VoteService voteService;
    private final VoterService voterService;

    public AdminController(ElectionService electionService, CandidateService candidateService, 
                          VoteService voteService, VoterService voterService) {
        this.electionService = electionService;
        this.candidateService = candidateService;
        this.voteService = voteService;
        this.voterService = voterService;
    }

    // Admin dashboard
    @GetMapping("")
    public String dashboard(Model model, @AuthenticationPrincipal Admin admin) {
        List<Election> allElections = electionService.getAllElections();
        long activeElections = allElections.stream()
            .filter(e -> e.getEndTime().isAfter(LocalDateTime.now()))
            .count();
        
        model.addAttribute("totalElections", allElections.size());
        model.addAttribute("activeElections", activeElections);
        model.addAttribute("completedElections", allElections.size() - activeElections);
        
        return "admin/dashboard";
    }

    // Manage Elections
    @GetMapping("/elections")
    public String manageElections(Model model) {
        List<Election> elections = electionService.getAllElections();
        // Calculate status for each election
        elections.forEach(Election::calculateStatus);
        model.addAttribute("elections", elections);
        return "admin/elections";
    }

    @GetMapping("/elections/new")
    public String newElection(Model model) {
        model.addAttribute("election", new Election());
        return "admin/election-form";
    }

    @PostMapping("/elections")
    public String createElection(@ModelAttribute Election election) {
        electionService.saveElection(election);
        return "redirect:/admin/elections?success";
    }

    @GetMapping("/elections/{id}/edit")
    public String editElection(@PathVariable Long id, Model model) {
        Election election = electionService.getElection(id)
            .orElseThrow(() -> new RuntimeException("Election not found"));
        model.addAttribute("election", election);
        return "admin/election-form";
    }

    @PostMapping("/elections/{id}")
    public String updateElection(@PathVariable Long id, @ModelAttribute Election election) {
        election.setId(id);
        electionService.saveElection(election);
        return "redirect:/admin/elections?updated";
    }

    @PostMapping("/elections/{id}/delete")
    public String deleteElection(@PathVariable Long id) {
        electionService.deleteElection(id);
        return "redirect:/admin/elections?deleted";
    }

    @GetMapping("/elections/{id}")
    public String viewElection(@PathVariable Long id, Model model) {
        Election election = electionService.getElection(id)
            .orElseThrow(() -> new RuntimeException("Election not found"));
        election.calculateStatus(); // Calculate status for UI
        List<Candidate> candidates = candidateService.getCandidatesByElection(election);
        model.addAttribute("election", election);
        model.addAttribute("candidates", candidates);
        return "admin/election-detail";
    }

    // Manage Candidates
    @GetMapping("/candidates")
    public String manageCandidates(Model model) {
        List<Candidate> candidates = candidateService.getAllCandidates();
        List<Election> elections = electionService.getAllElections();
        model.addAttribute("candidates", candidates);
        model.addAttribute("elections", elections);
        return "admin/candidates";
    }

    @GetMapping("/candidates/new")
    public String newCandidate(Model model) {
        List<Election> elections = electionService.getAllElections();
        model.addAttribute("candidate", new Candidate());
        model.addAttribute("elections", elections);
        return "admin/candidate-form";
    }

    @PostMapping("/candidates")
    public String createCandidate(@ModelAttribute Candidate candidate) {
        candidateService.saveCandidate(candidate);
        return "redirect:/admin/candidates?success";
    }

    @GetMapping("/candidates/{id}/edit")
    public String editCandidate(@PathVariable Long id, Model model) {
        Candidate candidate = candidateService.getCandidate(id)
            .orElseThrow(() -> new RuntimeException("Candidate not found"));
        List<Election> elections = electionService.getAllElections();
        model.addAttribute("candidate", candidate);
        model.addAttribute("elections", elections);
        return "admin/candidate-form";
    }

    @PostMapping("/candidates/{id}")
    public String updateCandidate(@PathVariable Long id, @ModelAttribute Candidate candidate) {
        candidate.setId(id);
        candidateService.saveCandidate(candidate);
        return "redirect:/admin/candidates?updated";
    }

    @PostMapping("/candidates/{id}/delete")
    public String deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return "redirect:/admin/candidates?deleted";
    }

    // Manage Voters
    @GetMapping("/voters")
    public String manageVoters(Model model) {
        // Implementation for voter management
        return "admin/voters";
    }

    // View Results
    @GetMapping("/results")
    public String viewResults(Model model) {
        List<Election> elections = electionService.getAllElections();
        model.addAttribute("elections", elections);
        return "admin/results";
    }

    // View candidates for a specific election
    @GetMapping("/elections/{electionId}/candidates")
    public String viewElectionCandidates(@PathVariable Long electionId, Model model) {
        Election election = electionService.getElection(electionId)
            .orElseThrow(() -> new RuntimeException("Election not found"));
        List<Candidate> candidates = candidateService.getCandidatesByElection(election);
        List<Election> allElections = electionService.getAllElections();
        
        model.addAttribute("candidates", candidates);
        model.addAttribute("elections", allElections);
        model.addAttribute("selectedElection", election);
        model.addAttribute("isElectionView", true);
        
        return "admin/candidates";
    }
}
