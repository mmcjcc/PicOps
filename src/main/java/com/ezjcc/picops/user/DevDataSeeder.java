package com.ezjcc.picops.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Dev-profile seed: guarantees the demo account exists so the app is usable
 * immediately without a signup/mail flow. Never active outside 'dev'.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DevDataSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.findByUsernameIgnoreCase("testuser").isEmpty()) {
            users.save(new User("testuser", "testuser@example.com", "Test User",
                encoder.encode("picops123")));
            log.info("Seeded dev account testuser/picops123");
        }
    }
}
