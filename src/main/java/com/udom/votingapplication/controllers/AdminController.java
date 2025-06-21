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
}
