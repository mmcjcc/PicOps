package com.ezjcc.picops.web;

import com.ezjcc.picops.user.User;
import com.ezjcc.picops.user.UserRepository;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentUser {

    private final UserRepository users;

    public CurrentUser(UserRepository users) {
        this.users = users;
    }

    /** The authenticated user; throws if the request is anonymous. */
    public User require(Principal principal) {
        User user = orNull(principal);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    /** The authenticated user, or null on public pages viewed anonymously. */
    public User orNull(Principal principal) {
        if (principal == null) {
            return null;
        }
        return users.findByUsernameIgnoreCase(principal.getName()).orElse(null);
    }

    public static String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
