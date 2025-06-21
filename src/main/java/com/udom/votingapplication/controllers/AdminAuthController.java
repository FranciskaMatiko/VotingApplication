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

    // Admin registration removed for security reasons
    // Only super admin can create admin accounts through the database directly
    // or through a future super admin panel
}
