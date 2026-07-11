package com.ezjcc.picops.picture;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;

/**
 * EXIF extraction and the privacy strip. Extraction failures are never
 * fatal: a photo with unreadable metadata still uploads, just without
 * the extracted fields.
 */
@Service
public class MetadataService {

    private final ObjectMapper json = new ObjectMapper();

    public record Extracted(Instant takenAt, String camera, Double lat, Double lon,
                            int orientation, String metaJson) {}

    public Extracted extract(byte[] bytes) {
        try {
            Metadata md = ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes));

            Instant takenAt = null;
            ExifSubIFDDirectory sub = md.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (sub != null) {
                Date d = sub.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (d != null) {
                    takenAt = d.toInstant();
                }
            }

            String camera = null;
            int orientation = 1;
            ExifIFD0Directory ifd0 = md.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0 != null) {
                String make = trim(ifd0.getString(ExifIFD0Directory.TAG_MAKE));
                String model = trim(ifd0.getString(ExifIFD0Directory.TAG_MODEL));
                if (model != null) {
                    camera = make != null && !model.startsWith(make) ? make + " " + model : model;
                } else {
                    camera = make;
                }
                if (camera != null && camera.length() > 120) {
                    camera = camera.substring(0, 120);
                }
                try {
                    orientation = ifd0.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                } catch (Exception ignored) {
                }
            }

            Double lat = null, lon = null;
            GpsDirectory gps = md.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps != null) {
                GeoLocation loc = gps.getGeoLocation();
                if (loc != null && !loc.isZero()) {
                    lat = loc.getLatitude();
                    lon = loc.getLongitude();
                }
            }

            Map<String, String> dump = new LinkedHashMap<>();
            for (Directory dir : md.getDirectories()) {
                for (Tag tag : dir.getTags()) {
                    String v = trim(tag.getDescription());
                    if (v != null && v.length() <= 500 && dump.size() < 400) {
                        dump.put(trim(dir.getName()) + " :: " + trim(tag.getTagName()), v);
                    }
                }
            }
            return new Extracted(takenAt, camera, lat, lon,
                orientation >= 1 && orientation <= 8 ? orientation : 1,
                json.writeValueAsString(dump));
        } catch (Exception e) {
            return new Extracted(null, null, null, null, 1, "{}");
        }
    }

    /** Applies the EXIF orientation so pixels match how the photo was shot. */
    public BufferedImage orient(BufferedImage img, int orientation) {
        if (orientation <= 1 || orientation > 8) {
            return img;
        }
        int w = img.getWidth(), h = img.getHeight();
        boolean swap = orientation >= 5;
        BufferedImage out = new BufferedImage(swap ? h : w, swap ? w : h,
            BufferedImage.TYPE_INT_RGB);
        AffineTransform t = new AffineTransform();
        switch (orientation) {
            case 2 -> { t.scale(-1, 1); t.translate(-w, 0); }
            case 3 -> { t.translate(w, h); t.rotate(Math.PI); }
            case 4 -> { t.scale(1, -1); t.translate(0, -h); }
            case 5 -> { t.rotate(Math.PI / 2); t.scale(1, -1); }
            case 6 -> { t.translate(h, 0); t.rotate(Math.PI / 2); }
            case 7 -> { t.scale(-1, 1); t.translate(-h, 0); t.translate(0, w); t.rotate(3 * Math.PI / 2); }
            case 8 -> { t.translate(0, w); t.rotate(3 * Math.PI / 2); }
        }
        Graphics2D g = out.createGraphics();
        g.drawImage(img, t, null);
        g.dispose();
        return out;
    }

    /**
     * Re-encodes the (already oriented) pixels, which drops every metadata
     * segment — EXIF, GPS, XMP, thumbnails. This is what non-owners are served.
     */
    public byte[] stripped(BufferedImage oriented, String contentType) throws Exception {
        String format = contentType.substring(contentType.indexOf('/') + 1);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        if (format.equals("jpeg")) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.92f);
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(b)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(oriented, null, null), param);
            } finally {
                writer.dispose();
            }
        } else {
            ImageIO.write(oriented, format, b);
        }
        return b.toByteArray();
    }

    /** Trims and removes NUL bytes — EXIF pads strings with \0, which
     *  PostgreSQL jsonb/text columns reject outright. */
    private static String trim(String s) {
        if (s == null) {
            return null;
        }
        String v = s.replace("\0", "").trim();
        return v.isEmpty() ? null : v;
    }
}
