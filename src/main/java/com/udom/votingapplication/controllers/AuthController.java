package com.udom.votingapplication.controllers;

import com.udom.votingapplication.models.Voter;
import com.udom.votingapplication.services.VoterService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final VoterService voterService;

    public AuthController(VoterService voterService) {
        this.voterService = voterService;
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("voter", new Voter());
        return "register";
    }

    @PostMapping("/register")
    public String process(@Valid @ModelAttribute("voter") Voter voter, BindingResult br) {
        if (br.hasErrors()) return "register";
        voterService.register(voter);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

