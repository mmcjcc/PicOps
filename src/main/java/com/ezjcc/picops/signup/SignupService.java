package com.ezjcc.picops.signup;

import com.ezjcc.picops.user.User;
import com.ezjcc.picops.user.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SignupService {

    private static final Logger log = LoggerFactory.getLogger(SignupService.class);
    private static final Pattern USERNAME = Pattern.compile("[a-zA-Z0-9_.-]{3,30}");
    private static final Duration TOKEN_TTL = Duration.ofHours(48);

    private final UserRepository users;
    private final ActivationTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final JavaMailSender mail;
    private final String baseUrl;
    private final String sender;

    public SignupService(UserRepository users, ActivationTokenRepository tokens,
                         PasswordEncoder encoder, JavaMailSender mail,
                         @Value("${picops.base-url}") String baseUrl,
                         @Value("${picops.mail-from}") String sender) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.mail = mail;
        this.baseUrl = baseUrl;
        this.sender = sender;
    }

    /** Returns an error message, or null on success. */
    public String signup(String username, String email, String displayName, String password) {
        if (username == null || !USERNAME.matcher(username).matches()) {
            return "Username must be 3-30 characters: letters, digits, . _ -";
        }
        if (email == null || !email.contains("@") || email.length() > 254) {
            return "A valid email address is required.";
        }
        if (displayName == null || displayName.isBlank() || displayName.length() > 60) {
            return "Display name is required (up to 60 characters).";
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            return "Password must be at least 8 characters.";
        }
        if (users.findByUsernameIgnoreCase(username).isPresent()) {
            return "That username is taken.";
        }
        User user = new User(username, email, displayName.trim(), encoder.encode(password));
        user.setEnabled(false);
        users.save(user);
        ActivationToken token = tokens.save(
            new ActivationToken(user, Instant.now().plus(TOKEN_TTL)));

        String link = baseUrl + "/activate?token=" + token.getToken();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(email);
            message.setSubject("Welcome to PicOps — activate your account");
            message.setText("Hello " + displayName.trim() + ",\n\n"
                + "Activate your PicOps account within 48 hours:\n\n" + link + "\n\n"
                + "If you didn't sign up, ignore this message.");
            mail.send(message);
        } catch (Exception e) {
            // dev-friendly fallback: the flow still works via the log
            log.warn("Mail send failed, activation link: {}", link, e);
        }
        return null;
    }

    /** Returns true when the token was valid and the account is now active. */
    public boolean activate(UUID tokenId) {
        return tokens.findById(tokenId).map(token -> {
            if (token.isExpired()) {
                tokens.delete(token);
                return false;
            }
            token.getUser().setEnabled(true);
            tokens.delete(token);
            return true;
        }).orElse(false);
    }
}
