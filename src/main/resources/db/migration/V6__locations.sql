-- Reverse-geocoded location, derived from GPS EXIF (owner-only, like the GPS
-- itself). City-level, from the offline GeoNames dataset in the ML sidecar.
ALTER TABLE pictures
    ADD COLUMN loc_city    varchar(120),
    ADD COLUMN loc_state   varchar(120),
    ADD COLUMN loc_country varchar(120);
