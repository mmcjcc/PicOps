# PicOps v2

Online Photo System — written in 2005 as a JSP/Hibernate/PostgreSQL learning
project, modernized in 2026 to Spring Boot 3 / Java 21 / Thymeleaf while
keeping the original ideas: albums, pictures stored in the database behind
application authorization, quotas, and comments.

- The untouched 2005 application: tag `legacy-2005`
- The 2005 app revived to run in Docker: branch `docker-revival`
- This branch: the modern rewrite, ported one vertical slice at a time

## Run it

```bash
docker compose up -d --build
```

Open http://localhost:8081 and sign in as `testuser` / `picops123`
(seeded by the `dev` profile; port 8080 is where the legacy app usually runs).

## Stack

Spring Boot 3.3 · Java 21 · Thymeleaf (+ htmx as slices land) · Spring
Security (delegating password encoder, bcrypt today) · Spring Data JPA ·
Flyway migrations · PostgreSQL 16 · GitHub Actions (build + CodeQL)

## Status

- [x] Slice 1 — scaffold, schema baseline, login/logout, seeded dev account, CI
- [x] Slice 2 — albums CRUD + visibility (private albums 404 for non-owners)
- [x] Slice 3 — upload (byte-sniff validation, quota), thumbnails, authorized image serving
- [x] Slice 4 — comments, search, public profile page (/u/username), prev/next photo nav
- [ ] Slice 5 — signup + mail (Mailpit), OAuth (Google), htmx polish (inline comments, drag-drop upload)
- [ ] Slice 6 — Azure deployment
