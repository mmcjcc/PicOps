package com.ezjcc.picops.user;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public DbUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("No such user: " + username));
        if (user.getPasswordHash() == null) {
            // external-provider account; password login not available
            throw new UsernameNotFoundException("No password login for: " + username);
        }
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPasswordHash())
            .disabled(!user.isEnabled())
            .authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))
            .build();
    }
}
