-- EXIF metadata (promoted columns for querying + full dump as JSONB) and the
-- privacy variant: clean_data is the metadata-stripped, orientation-applied
-- image served to everyone except the album owner.
ALTER TABLE pictures
    ADD COLUMN taken_at    timestamptz,
    ADD COLUMN camera      varchar(120),
    ADD COLUMN gps_lat     double precision,
    ADD COLUMN gps_lon     double precision,
    ADD COLUMN orientation smallint,
    ADD COLUMN meta        jsonb,
    ADD COLUMN clean_data  bytea;

CREATE INDEX ix_pictures_taken ON pictures (taken_at);
