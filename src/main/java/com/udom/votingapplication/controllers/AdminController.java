package com.udom.votingapplication.controllers;

import com.udom.votingapplication.models.*;
import com.udom.votingapplication.services.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.ColorConstants;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final ElectionService electionService;
    private final CandidateService candidateService;
    private final VoteService voteService;
    private final VoterService voterService;
    private final ResultService resultService;

    public AdminController(ElectionService electionService, CandidateService candidateService,
                          VoteService voteService, VoterService voterService, ResultService resultService) {
        this.electionService = electionService;
        this.candidateService = candidateService;
        this.voteService = voteService;
        this.voterService = voterService;
        this.resultService = resultService;
    }

    // Admin dashboard
    @GetMapping("")
    public String dashboard(Model model, @AuthenticationPrincipal Admin admin) {
        // Get all elections and calculate statuses
        List<Election> allElections = electionService.getAllElections();
        
        // Calculate status for each election
        for (Election election : allElections) {
            election.calculateStatus();
        }
        
        // Calculate statistics
        long totalElections = allElections.size();
        long activeElections = allElections.stream()
            .filter(e -> "active".equals(e.getStatus()))
            .count();
        long completedElections = allElections.stream()
            .filter(e -> "completed".equals(e.getStatus()))
            .count();
        long upcomingElections = allElections.stream()
            .filter(e -> "upcoming".equals(e.getStatus()))
            .count();
        
        // Get total voters
        long totalVoters = voterService.getAllVoters().size();
        
        // Get total candidates
        long totalCandidates = candidateService.getAllCandidates().size();
        
        // Get recent elections (last 5, sorted by creation/start time)
        List<Election> recentElections = allElections.stream()
            .sorted((e1, e2) -> e2.getStartTime().compareTo(e1.getStartTime()))
            .limit(5)
            .collect(Collectors.toList());
        
        // Add attributes to model
        model.addAttribute("totalElections", totalElections);
        model.addAttribute("activeElections", activeElections);
        model.addAttribute("completedElections", completedElections);
        model.addAttribute("upcomingElections", upcomingElections);
        model.addAttribute("totalVoters", totalVoters);
        model.addAttribute("totalCandidates", totalCandidates);
        model.addAttribute("recentElections", recentElections);
        
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
    public String manageVoters(Model model, @RequestParam(required = false) String search) {
        List<Voter> voters;
        if (search != null && !search.trim().isEmpty()) {
            voters = voterService.searchVoters(search);
        } else {
            voters = voterService.getAllVoters();
        }
        model.addAttribute("voters", voters);
        model.addAttribute("searchQuery", search);
        return "admin/voters";
    }

    @GetMapping("/voters/new")
    public String createVoter(Model model) {
        model.addAttribute("voter", new Voter());
        model.addAttribute("isEdit", false);
        return "admin/voter-form";
    }

    @PostMapping("/voters")
    public String saveVoter(@ModelAttribute Voter voter, Model model) {
        try {
            // Check if username already exists
            if (voterService.existsByUsername(voter.getUsername())) {
                model.addAttribute("voter", voter);
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Username already exists");
                return "admin/voter-form";
            }
            
            // Validate required fields
            if (voter.getUsername() == null || voter.getUsername().trim().isEmpty()) {
                model.addAttribute("voter", voter);
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Username is required");
                return "admin/voter-form";
            }
            
            if (voter.getFullName() == null || voter.getFullName().trim().isEmpty()) {
                model.addAttribute("voter", voter);
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Full name is required");
                return "admin/voter-form";
            }
            
            if (voter.getPassword() == null || voter.getPassword().trim().isEmpty()) {
                model.addAttribute("voter", voter);
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Password is required");
                return "admin/voter-form";
            }
            
            voterService.saveVoter(voter);
            return "redirect:/admin/voters?success";
        } catch (Exception e) {
            model.addAttribute("voter", voter);
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Error saving voter: " + e.getMessage());
            return "admin/voter-form";
        }
    }

    @GetMapping("/voters/{id}/edit")
    public String editVoter(@PathVariable Long id, Model model) {
        Voter voter = voterService.getVoter(id)
            .orElseThrow(() -> new RuntimeException("Voter not found"));
        model.addAttribute("voter", voter);
        model.addAttribute("isEdit", true);
        return "admin/voter-form";
    }

    @PostMapping("/voters/{id}")
    public String updateVoter(@PathVariable Long id, @ModelAttribute Voter voter, Model model) {
        try {
            // Get existing voter
            Voter existingVoter = voterService.getVoter(id)
                .orElseThrow(() -> new RuntimeException("Voter not found"));
            
            // Check if username is being changed and if it already exists
            if (!existingVoter.getUsername().equals(voter.getUsername()) && 
                voterService.existsByUsername(voter.getUsername())) {
                model.addAttribute("voter", voter);
                model.addAttribute("isEdit", true);
                model.addAttribute("error", "Username already exists");
                return "admin/voter-form";
            }
            
            // Validate required fields
            if (voter.getUsername() == null || voter.getUsername().trim().isEmpty()) {
                model.addAttribute("voter", voter);
                model.addAttribute("isEdit", true);
                model.addAttribute("error", "Username is required");
                return "admin/voter-form";
            }
            
            if (voter.getFullName() == null || voter.getFullName().trim().isEmpty()) {
                model.addAttribute("voter", voter);
                model.addAttribute("isEdit", true);
                model.addAttribute("error", "Full name is required");
                return "admin/voter-form";
            }
            
            voter.setId(id);
            
            // If password is empty, keep the existing password
            if (voter.getPassword() == null || voter.getPassword().trim().isEmpty()) {
                voter.setPassword(existingVoter.getPassword());
            }
            
            voterService.saveVoter(voter);
            return "redirect:/admin/voters?updated";
        } catch (Exception e) {
            model.addAttribute("voter", voter);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Error updating voter: " + e.getMessage());
            return "admin/voter-form";
        }
    }

    @PostMapping("/voters/{id}/delete")
    public String deleteVoter(@PathVariable Long id) {
        voterService.deleteVoter(id);
        return "redirect:/admin/voters?deleted";
    }

    // View Results
    @GetMapping("/results")
    public String viewResults(Model model) {
        List<ElectionResult> electionResults = resultService.getAllElectionResults();
        
        // Add debugging information
        List<Election> allElections = electionService.getAllElections();
        System.out.println("Total elections found: " + allElections.size());
        
        for (Election election : allElections) {
            election.calculateStatus();
            System.out.println("Election: " + election.getName() + ", Status: " + election.getStatus());
        }
        
        System.out.println("Election results size: " + electionResults.size());
        
        model.addAttribute("electionResults", electionResults);
        model.addAttribute("totalElections", allElections.size());
        
        return "admin/results";
    }
    
    // View detailed results for a specific election
    @GetMapping("/results/{electionId}")
    public String viewElectionResults(@PathVariable Long electionId, Model model) {
        Election election = electionService.getElection(electionId)
            .orElseThrow(() -> new RuntimeException("Election not found"));
        
        ElectionResult electionResult = resultService.getElectionResult(election);
        List<CandidateResult> candidateResults = resultService.getCandidateResults(electionId);
        
        model.addAttribute("election", election);
        model.addAttribute("electionResult", electionResult);
        model.addAttribute("candidateResults", candidateResults);
        
        return "admin/election-results";
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

    // Export detailed election results as PDF
    @GetMapping("/results/{electionId}/export")
    public ResponseEntity<byte[]> exportElectionResultsPDF(@PathVariable Long electionId) {
        try {
            Election election = electionService.getElection(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));
            
            ElectionResult electionResult = resultService.getElectionResult(election);
            List<CandidateResult> candidateResults = resultService.getCandidateResults(electionId);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Set font
            PdfFont font = PdfFontFactory.createFont();
            PdfFont boldFont = PdfFontFactory.createFont();
            
            // Title
            Paragraph title = new Paragraph("Election Results Report")
                .setFont(boldFont)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
            document.add(title);
            
            // Election name
            Paragraph electionName = new Paragraph(election.getName())
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
            document.add(electionName);
            
            // Date generated
            Paragraph dateGenerated = new Paragraph("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(dateGenerated);
            
            // Summary statistics
            Paragraph summaryTitle = new Paragraph("Election Summary")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
            document.add(summaryTitle);
            
            // Summary table
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            summaryTable.setWidth(UnitValue.createPercentValue(100));
            
            summaryTable.addCell(new Cell().add(new Paragraph("Total Votes:").setFont(font)));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(electionResult.getTotalVotes())).setFont(font)));
            
            summaryTable.addCell(new Cell().add(new Paragraph("Total Candidates:").setFont(font)));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(electionResult.getTotalCandidates())).setFont(font)));
            
            summaryTable.addCell(new Cell().add(new Paragraph("Eligible Voters:").setFont(font)));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(electionResult.getTotalEligibleVoters())).setFont(font)));
            
            summaryTable.addCell(new Cell().add(new Paragraph("Participation Rate:").setFont(font)));
            summaryTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", electionResult.getParticipationRate())).setFont(font)));
            
            document.add(summaryTable);
            document.add(new Paragraph(" ").setMarginBottom(20));
            
            // Winner section
            if (electionResult.getWinner() != null) {
                Paragraph winnerTitle = new Paragraph("Election Winner")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginBottom(10);
                document.add(winnerTitle);
                
                Paragraph winnerInfo = new Paragraph(
                    "Winner: " + electionResult.getWinner().getName() + 
                    " (" + electionResult.getWinner().getParty() + ")")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setMarginBottom(20);
                document.add(winnerInfo);
            }
            
            // Detailed results
            Paragraph resultsTitle = new Paragraph("Detailed Results")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
            document.add(resultsTitle);
            
            // Results table
            Table resultsTable = new Table(UnitValue.createPercentArray(new float[]{10, 30, 20, 20, 15, 15}));
            resultsTable.setWidth(UnitValue.createPercentValue(100));
            
            // Header row
            resultsTable.addHeaderCell(new Cell().add(new Paragraph("Rank").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            resultsTable.addHeaderCell(new Cell().add(new Paragraph("Candidate").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            resultsTable.addHeaderCell(new Cell().add(new Paragraph("Party").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            resultsTable.addHeaderCell(new Cell().add(new Paragraph("Position").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            resultsTable.addHeaderCell(new Cell().add(new Paragraph("Votes").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            resultsTable.addHeaderCell(new Cell().add(new Paragraph("Percentage").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            // Data rows
            for (int i = 0; i < candidateResults.size(); i++) {
                CandidateResult candidate = candidateResults.get(i);
                
                resultsTable.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)).setFont(font)));
                resultsTable.addCell(new Cell().add(new Paragraph(candidate.getCandidateName()).setFont(font)));
                resultsTable.addCell(new Cell().add(new Paragraph(candidate.getParty() != null ? candidate.getParty() : "Independent").setFont(font)));
                resultsTable.addCell(new Cell().add(new Paragraph(candidate.getPosition() != null ? candidate.getPosition() : "N/A").setFont(font)));
                resultsTable.addCell(new Cell().add(new Paragraph(String.valueOf(candidate.getVoteCount())).setFont(font)));
                resultsTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", candidate.getPercentage())).setFont(font)));
            }
            
            document.add(resultsTable);
            
            // Footer
            document.add(new Paragraph(" ").setMarginTop(20));
            Paragraph footer = new Paragraph("This is an official election results report generated by the Voting System.")
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
            document.add(footer);
            
            document.close();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "election-results-" + election.getName().replaceAll("[^a-zA-Z0-9]", "-") + ".pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
