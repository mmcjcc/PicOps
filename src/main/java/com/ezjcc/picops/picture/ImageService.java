package com.ezjcc.picops.picture;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;

/**
 * Image validation and thumbnailing. Same philosophy as the 2005 upload
 * pipeline: never trust the client's content type — sniff the actual bytes
 * with ImageIO and reject anything that doesn't decode.
 */
@Service
public class ImageService {

    private static final Set<String> ALLOWED = Set.of("jpeg", "png", "gif", "bmp");
    private static final int THUMB_WIDTH = 480;
    private static final float THUMB_QUALITY = 0.8f;

    public record Validated(byte[] bytes, String contentType, BufferedImage image) {}

    /** Returns null when the bytes are not a decodable image in an allowed format. */
    public Validated validate(byte[] bytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase();
                if (format.equals("jpg")) {
                    format = "jpeg";
                }
                if (!ALLOWED.contains(format)) {
                    return null;
                }
                reader.setInput(iis);
                BufferedImage image = reader.read(0);
                return new Validated(bytes, "image/" + format, image);
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /** JPEG thumbnail, {@value #THUMB_WIDTH}px wide (or original width if smaller). */
    public byte[] thumbnailJpeg(BufferedImage src) throws Exception {
        int tw = Math.min(THUMB_WIDTH, src.getWidth());
        int th = Math.max(1, Math.round((float) src.getHeight() * tw / src.getWidth()));
        BufferedImage out = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, tw, th, null);
        g.dispose();

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(THUMB_QUALITY);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(b)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(out, null, null), param);
        } finally {
            writer.dispose();
        }
        return b.toByteArray();
    }
}
