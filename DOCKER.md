# Running PicOps (2005 edition) in Docker

The original 2005 JSP/Hibernate/PostgreSQL app, revived to run on modern
container infrastructure. See git history for the changes that were required
(Oracle-only JPEG codec -> javax.imageio, PostgreSQL 8.1 JDBC3 driver ->
pgjdbc 42.7.7, `binary`/`numeric` column types -> `bytea`/`varchar`).

## Quickstart

```bash
docker compose up -d --build          # Tomcat 9 / JDK 8 app + PostgreSQL 16
docker compose exec -T db psql -U postgres -d PicOps < docker/seed.sql
```

Then open http://localhost:8080 and log in as `testuser` / `picops123`.

- The schema is created automatically on first app startup (`hbm2ddl.auto=update`).
- Pictures live in the `picops_pgdata` named volume; `docker compose down` keeps
  it, `docker compose down -v` wipes it.
- New-user signup requires a reachable SMTP server for the activation email and
  will fail (and roll back the user) until one is added to the compose stack.
- Do not expose this app beyond localhost: it is intentionally preserved
  2005-era code with known-vulnerable dependencies, kept as a security-scanning
  and modernization baseline.

## Windows / WSL2 notes

Docker Engine runs inside WSL2 (no Docker Desktop needed). Set a long
`vmIdleTimeout` in `%USERPROFILE%\.wslconfig` or keep a WSL terminal open,
otherwise Windows shuts the WSL VM (and the containers) down after ~60 idle
seconds. Keep the Postgres volume on the WSL ext4 filesystem — never bind-mount
a database onto /mnt/c.
