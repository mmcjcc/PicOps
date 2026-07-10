# PicOps — Online Photo System (2005, revived for Docker)

PicOps is a photo-album web application written in 2005 as a Java
web-development learning project — JSP + JSTL, Hibernate 3 (back when ORM
was the new idea), and PostgreSQL, built in NetBeans. Design goals of the
original: albums with public/private visibility, per-user storage quotas,
comments, slideshows, and pictures stored **in the database** behind
application authorization rather than as web-servable files — a security
decision that aged well.

**This branch** is the 2005 code brought back to life in 2026 with the
minimum changes needed to run on modern infrastructure (Tomcat 9 / JDK 8 /
PostgreSQL 16 in Docker). See [DOCKER.md](DOCKER.md) for the quickstart and
the list of what had to change (Oracle-only JPEG codec, ancient JDBC driver,
a few PostgreSQL type mismatches).

## The branches tell the story

| Ref | What it is |
|---|---|
| tag `legacy-2005` | The untouched original, exactly as written in 2005 |
| branch `docker-revival` (this one) | The 2005 app, minimally patched to run in Docker |
| branch `main` | The modern rewrite: Spring Boot 3 / Java 21 / Thymeleaf |

## A second career as a security-scanning target

The app was never updated after 2005, which later made it a useful live
demo for static analysis and software-composition-analysis tooling (a
Fortify `.fpr` from one such run is in the repo, and Dependabot has
opinions about `commons-fileupload 1.0`). That role is intentional and
preserved: **do not deploy this branch anywhere reachable from the
internet.** Run it on localhost, enjoy the filmstrip slideshow, and compare
it against `main` to see twenty years of AppSec evolution in one repo.
