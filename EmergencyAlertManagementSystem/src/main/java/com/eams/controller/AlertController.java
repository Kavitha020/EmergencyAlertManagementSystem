package com.eams.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eams.model.Alert;
import com.eams.model.User;
import com.eams.repository.AlertTypeRepository;
import com.eams.service.AlertService;
import com.eams.service.UserService;

@Controller
public class AlertController {
	
	@Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @Autowired
    private AlertTypeRepository alertTypeRepository;

    @GetMapping("/user/create-alert")
    public String showCreateAlertForm(Model model) {
        model.addAttribute("alert", new Alert());
        model.addAttribute("alertTypes", alertTypeRepository.findAll());
        return "create_alert";
    }

    @PostMapping("/user/create-alert")
    public String createAlert(@ModelAttribute Alert alert, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        alertService.createAlert(alert, user);
        return "redirect:/dashboard";
    }

    @GetMapping("/anonymous")
    public String showAnonymousAlertForm(Model model) {
        model.addAttribute("alert", new Alert());
        model.addAttribute("alertTypes", alertTypeRepository.findAll());
        return "anonymous_alert";
    }

    @PostMapping("/anonymous")
    public String createAnonymousAlert(@ModelAttribute Alert alert, Model model) {
        Alert savedAlert = alertService.createAlert(alert, null);
        model.addAttribute("token", savedAlert.getAnonymousToken());
        return "check_status";
    }

    @GetMapping("/check-status")
    public String showCheckStatusForm() {
        return "check_status";
    }

    @PostMapping("/check-status")
    public String checkStatus(@RequestParam("token") String token, Model model) {
        Alert alert = alertService.getAlertByToken(token);
        model.addAttribute("alert", alert);
        return "check_status";
    }

    @PostMapping("/responder/accept")
    public String acceptAlert(@RequestParam("alertId") Long alertId, Authentication authentication) {
        User responder = userService.findByUsername(authentication.getName());
        alertService.acceptAlert(alertId, responder);
        return "redirect:/dashboard";
    }

    @PostMapping("/responder/reject")
    public String rejectAlert(@RequestParam("alertId") Long alertId, Authentication authentication) {
        User responder = userService.findByUsername(authentication.getName());
        alertService.rejectAlert(alertId, responder);
        return "redirect:/dashboard";
    }

    @PostMapping("/responder/resolve")
    public String resolveAlert(@RequestParam("alertId") Long alertId, Authentication authentication) {
        User responder = userService.findByUsername(authentication.getName());
        alertService.resolveAlert(alertId, responder);
        return "redirect:/dashboard";
    }

	
}
