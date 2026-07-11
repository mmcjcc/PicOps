-- CLIP embeddings (pgvector) and zero-shot tags produced by the ML sidecar.
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE picture_embeddings (
    picture_id uuid PRIMARY KEY REFERENCES pictures (id) ON DELETE CASCADE,
    embedding  vector(512) NOT NULL
);

CREATE TABLE picture_tags (
    picture_id uuid NOT NULL REFERENCES pictures (id) ON DELETE CASCADE,
    tag        varchar(40) NOT NULL,
    score      real        NOT NULL,
    PRIMARY KEY (picture_id, tag)
);
CREATE INDEX ix_tags_tag ON picture_tags (tag);
