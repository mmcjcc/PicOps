package com.ezjcc.picops.signup;

import com.ezjcc.picops.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activation_tokens")
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected ActivationToken() {
    }

    public ActivationToken(User user, Instant expiresAt) {
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public UUID getToken() { return token; }
    public User getUser() { return user; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}
