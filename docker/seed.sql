-- PicOps dev-environment seed data.
-- Run AFTER first app startup (Hibernate hbm2ddl creates the tables):
--   docker compose exec -T db psql -U postgres -d PicOps < docker/seed.sql
--
-- Passwords/answers are stored as base64(sha1(text)), matching UserUtil.encrypt().
-- The secret-question answer is lowercased before hashing, matching handleRegistration().

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Site config. NOTE: SiteConfigUtil hard-codes loading the row with id '0'.
-- quota is total per-user storage in MB. mailserver is a placeholder until an
-- SMTP container (e.g. Mailpit) is added; until then, new-user signup will fail
-- at the activation-email step and roll the user back.
INSERT INTO siteconfig (id, senderemail, webserver, mailserver, quota)
SELECT '0', 'picops@example.com', 'http://localhost:8080', 'localhost', 100
WHERE NOT EXISTS (SELECT 1 FROM siteconfig);

-- Test account: username=testuser  password=picops123  secret answer=blue
INSERT INTO users (id, name_first, name_last, username, email, password,
                   questionid, answer, validated, registrationdate)
SELECT md5('picops-test-user-1'),
       'Test', 'User', 'testuser', 'testuser@example.com',
       encode(digest('picops123', 'sha1'), 'base64'),
       1,
       encode(digest('blue', 'sha1'), 'base64'),
       true,
       now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');
