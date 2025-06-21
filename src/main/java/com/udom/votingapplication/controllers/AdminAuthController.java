package com.udom.votingapplication.controllers;

import com.udom.votingapplication.models.Admin;
import com.udom.votingapplication.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("admin", new Admin());
        return "admin/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("admin") Admin admin, BindingResult br) {
        if (br.hasErrors()) return "admin/register";
        adminService.register(admin);
        return "redirect:/login?registered";
    }
}
