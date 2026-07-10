package com.ezjcc.picops.signup;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, UUID> {
}
