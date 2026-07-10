package com.ezjcc.picops.web;

import com.ezjcc.picops.signup.SignupService;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignupController {

    private final SignupService signup;

    public SignupController(SignupService signup) {
        this.signup = signup;
    }

    @GetMapping("/signup")
    public String form() {
        return "signup";
    }

    @PostMapping("/signup")
    public String submit(@RequestParam String username, @RequestParam String email,
                         @RequestParam String displayName, @RequestParam String password,
                         Model model) {
        String error = signup.signup(username, email, displayName, password);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("displayName", displayName);
            return "signup";
        }
        model.addAttribute("email", email);
        return "signup-sent";
    }

    @GetMapping("/activate")
    public String activate(@RequestParam UUID token) {
        return signup.activate(token)
            ? "redirect:/login?activated"
            : "redirect:/login?activationfailed";
    }
}
