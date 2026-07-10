-- One-time account activation tokens (successor to the 2005 email-activation flow).
CREATE TABLE activation_tokens (
    token      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at timestamptz NOT NULL
);
CREATE INDEX ix_activation_user ON activation_tokens (user_id);
