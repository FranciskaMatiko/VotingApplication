package com.udom.votingapplication.controllers;

import com.udom.votingapplication.models.*;
import com.udom.votingapplication.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/voter")
public class VoterController {
    private final ElectionService electionService;
    private final CandidateService candidateService;
    private final VoteService voteService;
    private final ResultService resultService;
    private final VoterService voterService;
    private final PdfService pdfService;

    public VoterController(ElectionService electionService, CandidateService candidateService, 
                          VoteService voteService, ResultService resultService, VoterService voterService,
                          PdfService pdfService) {
        this.electionService = electionService;
        this.candidateService = candidateService;
        this.voteService = voteService;
        this.resultService = resultService;
        this.voterService = voterService;
        this.pdfService = pdfService;
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

    // View all results
    @GetMapping("/results")
    public String allResults(Model model, @AuthenticationPrincipal Voter voter) {
        List<Election> allElections = electionService.getAllElections();
        
        // Filter elections with visible results
        List<Election> electionsWithResults = allElections.stream()
            .filter(election -> {
                election.calculateStatus();
                return election.isResultsVisible() && "completed".equals(election.getStatus());
            })
            .collect(Collectors.toList());
        
        // Get election results with statistics
        List<ElectionResult> electionResults = electionsWithResults.stream()
            .map(election -> resultService.getElectionResult(election))
            .collect(Collectors.toList());
        
        // Count statistics for dashboard
        long totalElections = allElections.size();
        long completedElections = allElections.stream()
            .filter(e -> {
                e.calculateStatus();
                return "completed".equals(e.getStatus());
            })
            .count();
        long votedElections = allElections.stream()
            .filter(e -> voteService.hasVoted(voter.getId(), e.getId()))
            .count();
        
        model.addAttribute("electionResults", electionResults);
        model.addAttribute("electionsWithResults", electionsWithResults);
        model.addAttribute("totalElections", totalElections);
        model.addAttribute("completedElections", completedElections);
        model.addAttribute("votedElections", votedElections);
        model.addAttribute("resultsAvailable", electionsWithResults.size());
        
        return "results";
    }

    // View results for specific election
    @GetMapping("/elections/{electionId}/results")
    public String electionResults(@PathVariable Long electionId, Model model, @AuthenticationPrincipal Voter voter) {
        Election election = electionService.getElection(electionId).orElseThrow();
        election.calculateStatus();
        
        if (!election.isResultsVisible() || !"completed".equals(election.getStatus())) {
            model.addAttribute("message", "Results are not available yet for this election.");
            model.addAttribute("election", election);
            return "election-results";
        }
        
        // Get detailed candidate results
        List<CandidateResult> candidateResults = resultService.getCandidateResults(electionId);
        ElectionResult electionResult = resultService.getElectionResult(election);
        
        // Check if voter voted in this election
        boolean voterVoted = voteService.hasVoted(voter.getId(), electionId);
        
        model.addAttribute("election", election);
        model.addAttribute("candidateResults", candidateResults);
        model.addAttribute("electionResult", electionResult);
        model.addAttribute("voterVoted", voterVoted);
        
        return "election-results";
    }

    // Voter profile page
    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal Voter voter) {
        model.addAttribute("voter", voter);
        
        // Calculate and add real-time voting statistics
        com.udom.votingapplication.models.VotingStatistics stats = voterService.getVotingStatistics(voter.getId());
        model.addAttribute("votingStats", stats);
        
        return "voter-profile";
    }

    // Change password endpoint
    @PostMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Voter voter) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            // Validate input
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Current password is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "New password is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "New password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Attempt to change password
            boolean success = voterService.changePassword(voter.getId(), currentPassword, newPassword);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Password changed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while changing password");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Update profile endpoint
    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Voter voter) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String fullName = request.get("fullName");
            String email = request.get("email");
            
            // Validate input
            if (fullName == null || fullName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Full name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Basic email validation
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                response.put("success", false);
                response.put("message", "Please enter a valid email address");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Attempt to update profile
            boolean success = voterService.updateProfile(voter.getId(), fullName.trim(), email.trim());
            
            if (success) {
                response.put("success", true);
                response.put("message", "Profile updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update profile");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while updating profile");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Update notification settings endpoint
    @PostMapping("/profile/notification-settings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(
            @RequestBody Map<String, Boolean> request,
            @AuthenticationPrincipal Voter voter) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean electionNotifications = request.getOrDefault("electionNotifications", false);
            boolean voteReminders = request.getOrDefault("voteReminders", false);
            boolean resultNotifications = request.getOrDefault("resultNotifications", false);
            boolean emailNotifications = request.getOrDefault("emailNotifications", false);
            
            // Attempt to update notification settings
            boolean success = voterService.updateNotificationSettings(voter.getId(), 
                electionNotifications, voteReminders, resultNotifications, emailNotifications);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Notification settings updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update notification settings");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while updating notification settings");
            return ResponseEntity.internalServerError().body(response);
        }
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

    // View election details
    @GetMapping("/elections/{electionId}")
    public String electionDetails(@PathVariable Long electionId, Model model, @AuthenticationPrincipal Voter voter) {
        try {
            System.out.println("Loading election details for ID: " + electionId);
            
            Election election = electionService.getElection(electionId).orElse(null);
            
            if (election == null) {
                System.out.println("Election not found with ID: " + electionId);
                return "redirect:/voter/elections?error=notFound";
            }
            
            System.out.println("Found election: " + election.getName());
            election.calculateStatus();
            
            boolean hasVoted = voteService.hasVoted(voter.getId(), electionId);
            System.out.println("Voter has voted: " + hasVoted);
            election.setVoterHasVoted(hasVoted);
            
            List<Candidate> candidates = candidateService.getCandidatesByElection(electionId);
            System.out.println("Found " + candidates.size() + " candidates");
            
            model.addAttribute("election", election);
            model.addAttribute("candidates", candidates);
            model.addAttribute("hasVoted", hasVoted);
            
            System.out.println("Returning election-details template");
            return "election-details";
        } catch (Exception e) {
            System.out.println("Error loading election details: " + e.getMessage());
            e.printStackTrace(); // For debugging
            return "redirect:/voter/elections?error=loadFailed";
        }
    }

    // Export voting history as PDF
    @GetMapping("/history/export/pdf")
    public ResponseEntity<byte[]> exportHistoryAsPDF(@AuthenticationPrincipal Voter voter) {
        try {
            // Get voter's voting history
            List<Election> allElections = electionService.getAllElections();
            List<Election> votedElections = allElections.stream()
                .filter(election -> voteService.hasVoted(voter.getId(), election.getId()))
                .collect(Collectors.toList());
            
            // Generate PDF using PdfService
            byte[] pdfBytes = pdfService.generateVotingHistoryPdf(voter, votedElections);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=voting-history.pdf")
                .body(pdfBytes);
            
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            // Return error as plain text
            String errorMessage = "Error generating PDF export: " + e.getMessage();
            return ResponseEntity.internalServerError()
                .header("Content-Type", "text/plain")
                .body(errorMessage.getBytes());
        }
    }

    // Export voting history as CSV
    @GetMapping("/history/export/csv")
    public ResponseEntity<byte[]> exportHistoryAsCSV(@AuthenticationPrincipal Voter voter) {
        try {
            // Get voter's voting history
            List<Election> allElections = electionService.getAllElections();
            List<Election> votedElections = allElections.stream()
                .filter(election -> voteService.hasVoted(voter.getId(), election.getId()))
                .collect(Collectors.toList());
            
            // Generate CSV content
            StringBuilder csvContent = new StringBuilder();
            
            // Add header with voter information
            csvContent.append("# Voting History Report\n");
            csvContent.append("# Voter: ").append(voter.getFullName()).append("\n");
            csvContent.append("# Username: ").append(voter.getUsername()).append("\n");
            csvContent.append("# Report Generated: ").append(LocalDateTime.now().toString()).append("\n");
            csvContent.append("# Total Elections Participated: ").append(votedElections.size()).append("\n");
            csvContent.append("\n");
            
            // Add CSV headers
            csvContent.append("Election Name,Election Type,End Date,Status\n");
            
            // Add data rows
            if (votedElections.isEmpty()) {
                csvContent.append("No voting history found,,,\n");
            } else {
                for (Election election : votedElections) {
                    election.calculateStatus();
                    csvContent.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCSV(election.getName() != null ? election.getName() : "N/A"),
                        escapeCSV(election.getVotingType() != null ? election.getVotingType() : "N/A"),
                        election.getEndTime() != null ? election.getEndTime().toString() : "N/A",
                        escapeCSV(election.getStatus() != null ? election.getStatus() : "N/A")
                    ));
                }
            }
            
            byte[] csvBytes = csvContent.toString().getBytes("UTF-8");
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=voting-history.csv")
                .body(csvBytes);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error generating CSV export".getBytes());
        }
    }
    
    // Helper method to escape CSV values
    private String escapeCSV(String value) {
        if (value == null) return "";
        // Escape quotes by doubling them
        return value.replace("\"", "\"\"");
    }
}
