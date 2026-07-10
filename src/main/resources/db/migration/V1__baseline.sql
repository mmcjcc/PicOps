-- PicOps v2 baseline schema.
-- Direct descendant of the 2005 .hbm.xml mappings (users, albums, images,
-- thumbnails, comments), with 2026 hygiene: real foreign keys, UUID keys,
-- timestamptz, and an auth model ready for OAuth providers.

CREATE TABLE users (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username      varchar(30)  NOT NULL,
    email         varchar(254) NOT NULL,
    display_name  varchar(60)  NOT NULL,
    -- null when the account comes from an external provider (Google, etc.)
    password_hash varchar(100),
    auth_provider varchar(20)  NOT NULL DEFAULT 'LOCAL',
    enabled       boolean      NOT NULL DEFAULT true,
    created_at    timestamptz  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_users_username ON users (lower(username));
CREATE UNIQUE INDEX ux_users_email    ON users (lower(email));

CREATE TABLE albums (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id         uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title            varchar(200) NOT NULL,
    description      varchar(500),
    visibility       varchar(10)  NOT NULL DEFAULT 'PRIVATE'
                     CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    cover_picture_id uuid,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX ix_albums_owner ON albums (owner_id);

CREATE TABLE pictures (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    album_id     uuid NOT NULL REFERENCES albums (id) ON DELETE CASCADE,
    data         bytea NOT NULL,          -- pictures live in the DB, behind app authz (2005 thesis, kept)
    content_type varchar(50)  NOT NULL,
    file_name    varchar(255) NOT NULL,
    size_bytes   bigint       NOT NULL,
    title        varchar(200),
    description  varchar(1000),
    taken_on     date,
    created_at   timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX ix_pictures_album ON pictures (album_id);

CREATE TABLE thumbnails (
    picture_id uuid PRIMARY KEY REFERENCES pictures (id) ON DELETE CASCADE,
    data       bytea NOT NULL
);

CREATE TABLE comments (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    picture_id uuid NOT NULL REFERENCES pictures (id) ON DELETE CASCADE,
    author_id  uuid REFERENCES users (id) ON DELETE SET NULL,
    body       varchar(1000) NOT NULL,
    created_at timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX ix_comments_picture ON comments (picture_id);

ALTER TABLE albums
    ADD CONSTRAINT fk_albums_cover
    FOREIGN KEY (cover_picture_id) REFERENCES pictures (id) ON DELETE SET NULL;
