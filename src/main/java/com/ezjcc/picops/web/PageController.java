package com.ezjcc.picops.web;

import com.ezjcc.picops.user.UserRepository;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final UserRepository users;

    public PageController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        users.findByUsernameIgnoreCase(principal.getName()).ifPresent(u -> {
            model.addAttribute("displayName", u.getDisplayName());
            model.addAttribute("initials", initials(u.getDisplayName()));
        });
        return "home";
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
