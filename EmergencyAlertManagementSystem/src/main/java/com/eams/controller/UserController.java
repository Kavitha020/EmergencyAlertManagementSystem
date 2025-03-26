package com.eams.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.eams.model.User;
import com.eams.service.UserService;

@Controller
public class UserController {
	
	@Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        user.setRole("USER"); // Default role
        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        if ("USER".equals(user.getRole())) {
            return "user_dashboard";
        } else if ("RESPONDER".equals(user.getRole())) {
            return "responder_dashboard";
        } else if ("ADMIN".equals(user.getRole())) {
            return "admin_dashboard";
        }
        return "redirect:/login";
    }

}
