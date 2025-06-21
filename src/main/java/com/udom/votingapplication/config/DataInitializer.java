package com.udom.votingapplication.config;

import com.udom.votingapplication.models.*;
import com.udom.votingapplication.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializer(ElectionRepository electionRepository, CandidateRepository candidateRepository,
                          AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no elections exist
        if (electionRepository.count() == 0) {
            initializeElections();
        }
        
        // Initialize default admin if no admin exists
        if (adminRepository.count() == 0) {
            initializeDefaultAdmin();
        }
    }
    
    private void initializeElections() {
        // Create Student Council Election
        Election studentCouncil = new Election();
        studentCouncil.setName("Student Council Election 2025");
        studentCouncil.setDescription("Choose your representatives for the upcoming academic year. Your voice matters in shaping our institution's future and student life.");
        studentCouncil.setStartTime(LocalDateTime.now().minusDays(1));
        studentCouncil.setEndTime(LocalDateTime.now().plusDays(7));
        studentCouncil.setResultsVisible(false);
        studentCouncil = electionRepository.save(studentCouncil);
        
        // Add candidates for Student Council
        createCandidate("Alice Johnson", "Computer Science student with 3 years experience in student government. Focused on improving campus facilities and student services.", studentCouncil);
        createCandidate("Bob Martinez", "Business Administration major passionate about student rights and academic excellence. Plans to enhance study spaces and library resources.", studentCouncil);
        createCandidate("Carol Kim", "Engineering student advocating for better technology infrastructure and sustainable campus initiatives.", studentCouncil);
        
        // Create Faculty Representative Election
        Election facultyRep = new Election();
        facultyRep.setName("Faculty Representative Election");
        facultyRep.setDescription("Select a student representative to work directly with faculty on curriculum and academic policy decisions.");
        facultyRep.setStartTime(LocalDateTime.now().minusDays(2));
        facultyRep.setEndTime(LocalDateTime.now().plusDays(5));
        facultyRep.setResultsVisible(false);
        facultyRep = electionRepository.save(facultyRep);
        
        // Add candidates for Faculty Representative
        createCandidate("David Chen", "Graduate student in Education with experience in academic committee work. Committed to bridging student-faculty communication.", facultyRep);
        createCandidate("Emma Wilson", "Psychology major with strong advocacy skills. Focused on mental health resources and academic support systems.", facultyRep);
        
        // Create Completed Election (Club President)
        Election clubPresident = new Election();
        clubPresident.setName("Computer Science Club President");
        clubPresident.setDescription("Leadership election for the Computer Science Club for the 2025 academic year.");
        clubPresident.setStartTime(LocalDateTime.now().minusDays(10));
        clubPresident.setEndTime(LocalDateTime.now().minusDays(3));
        clubPresident.setResultsVisible(true);
        clubPresident = electionRepository.save(clubPresident);
        
        // Add candidates for Club President
        createCandidate("Frank Rodriguez", "Senior CS student with programming competition experience. Plans to organize more coding workshops and industry networking events.", clubPresident);
        createCandidate("Grace Taylor", "Junior CS student focused on diversity and inclusion in tech. Aims to create mentorship programs for underrepresented students.", clubPresident);
        
        System.out.println("Sample elections and candidates initialized successfully!");
    }
    
    private void createCandidate(String name, String profile, Election election) {
        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setProfile(profile);
        candidate.setElection(election);
        candidateRepository.save(candidate);
    }
    
    private void initializeDefaultAdmin() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Administrator");
        admin.setEmail("admin@votingsystem.com");
        admin.setEnabled(true);
        adminRepository.save(admin);
        
        System.out.println("Default admin created: username=admin, password=admin123");
    }
}
