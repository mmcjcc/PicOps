# PicOps — Online Photo System (2005)

A photo-album web application written in 2005 as a Java web-development
learning project: JSP + JSTL, Hibernate 3 ORM, and PostgreSQL, built in
NetBeans, in the era before Flickr and Google Photos settled the category.
Features: user accounts with email activation, albums with public/private
visibility, multi-file upload with server-side validation, generated
thumbnails, slideshows, comments, search, and per-user storage quotas —
with all pictures stored in the database behind application authorization
rather than on a web-servable filesystem.

**This branch is a time capsule.** Apart from this README, it preserves the
application exactly as written in 2005 (the pristine state is also tagged
`legacy-2005`). It compiles against Java 1.4-era APIs, expects Oracle's
proprietary JPEG codec, and bundles dependencies with well-known CVEs.
That is deliberate: since the 2010s this codebase has served as a live
target for demonstrating security-scanning tools — the Fortify scan result
in the repo and the standing Dependabot alerts are part of the exhibit.

**Do not deploy this code anywhere reachable from the internet.**

## Looking for a version that runs?

| Ref | What it is |
|---|---|
| branch `main` | The modern rewrite — Spring Boot 3 / Java 21 / Thymeleaf, actively developed |
| branch `docker-revival` | This 2005 app, minimally patched to run under Docker (Tomcat 9 / PostgreSQL 16) |
| tag `legacy-2005` | This branch's pristine state, pre-README |
