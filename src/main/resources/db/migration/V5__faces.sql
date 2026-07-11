-- Face detection results and per-owner people clusters.
CREATE TABLE people (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id   uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name       varchar(60),
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_people_owner ON people (owner_id);

CREATE TABLE faces (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    picture_id uuid NOT NULL REFERENCES pictures (id) ON DELETE CASCADE,
    person_id  uuid REFERENCES people (id) ON DELETE SET NULL,
    -- bbox in thumbnail coordinate space (faces are detected on thumbnails)
    x1 int NOT NULL, y1 int NOT NULL, x2 int NOT NULL, y2 int NOT NULL,
    det_score  real NOT NULL,
    embedding  vector(512) NOT NULL
);
CREATE INDEX ix_faces_picture ON faces (picture_id);
CREATE INDEX ix_faces_person ON faces (person_id);

ALTER TABLE pictures ADD COLUMN faces_scanned boolean NOT NULL DEFAULT false;
