import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Random;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Dev-only sample-data seeder for the legacy PicOps schema.
 * Generates random abstract "photos" with ImageIO (same library the app's
 * upload pipeline uses), thumbnails them the same way createThubnail() does
 * (132px wide, height proportional, capped at 150), and inserts albums,
 * images, and thumbnails for the seeded test account.
 *
 * Run inside the app container:
 *   javac -cp <pgjdbc.jar> Seeder.java && java -cp .:<pgjdbc.jar> Seeder
 */
public class Seeder {

    static final String[][] ALBUMS = {
        // title, visibility, description, photo count
        {"Beach Escape",    "public",  "Sun, sand, and generated surf", "6"},
        {"Mountain Hikes",  "public",  "Synthetic summits in every frame", "7"},
        {"City Nights",     "private", "Late-night gradients downtown", "5"},
        {"Garden Log",      "private", "The backyard, procedurally in bloom", "6"},
    };

    // one loose color theme per album so covers look distinct
    static final Color[][] THEMES = {
        {new Color(0x2E86C1), new Color(0xF7DC9F), new Color(0xF0B27A)},
        {new Color(0x1B4F3F), new Color(0x82B366), new Color(0xD5E8D4)},
        {new Color(0x1A1A3E), new Color(0x6C3483), new Color(0xE59866)},
        {new Color(0x557A46), new Color(0xF2E8C6), new Color(0xC08261)},
    };

    public static void main(String[] args) throws Exception {
        Random rnd = new Random(20051126); // deterministic: the app's birthday
        Connection c = DriverManager.getConnection(
            "jdbc:postgresql://db:5432/PicOps", "postgres", "!postgres!");
        c.setAutoCommit(false);

        String ownerId;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM users WHERE username='testuser'");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) { System.out.println("testuser not found - run seed.sql first"); return; }
            ownerId = rs.getString(1);
        }

        PreparedStatement insAlbum = c.prepareStatement(
            "INSERT INTO albums (id, ownerid, icon, title, albumvisibility, photographer, description, creationdate) " +
            "VALUES (?,?,?,?,?,?,?,?)");
        PreparedStatement insPic = c.prepareStatement(
            "INSERT INTO images (id, albumid, image, imagetype, timestamp, imagetitle, filename, imagesize, imagedescription, imagephotographer, imagedate) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        PreparedStatement insThumb = c.prepareStatement(
            "INSERT INTO thumbnails (id, image) VALUES (?,?)");

        int albumCount = 0, picCount = 0;
        for (int a = 0; a < ALBUMS.length; a++) {
            String title = ALBUMS[a][0];
            try (PreparedStatement chk = c.prepareStatement(
                    "SELECT 1 FROM albums WHERE ownerid=? AND title=?")) {
                chk.setString(1, ownerId); chk.setString(2, title);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) { System.out.println("skip (exists): " + title); continue; }
                }
            }
            int photos = Integer.parseInt(ALBUMS[a][3]);
            String albumId = newId();
            String iconPicId = null;

            for (int p = 0; p < photos; p++) {
                String picId = newId();
                if (p == 0) iconPicId = picId;
                BufferedImage img = makePhoto(THEMES[a], rnd);
                byte[] full = jpeg(img);
                byte[] thumb = jpeg(thumbnail(img));

                insPic.setString(1, picId);
                insPic.setString(2, albumId);
                insPic.setBytes(3, full);
                insPic.setString(4, "JPEG");
                insPic.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                insPic.setString(6, title + " #" + (p + 1));
                insPic.setString(7, title.toLowerCase().replace(' ', '-') + "-" + (p + 1) + ".jpg");
                insPic.setLong(8, full.length);
                insPic.setString(9, "Seeded sample image");
                insPic.setString(10, "Seed Bot");
                insPic.setString(11, "7/10/2026");
                insPic.executeUpdate();

                insThumb.setString(1, picId);
                insThumb.setBytes(2, thumb);
                insThumb.executeUpdate();
                picCount++;
            }

            insAlbum.setString(1, albumId);
            insAlbum.setString(2, ownerId);
            insAlbum.setString(3, iconPicId);
            insAlbum.setString(4, title);
            insAlbum.setString(5, ALBUMS[a][1]);
            insAlbum.setString(6, "Seed Bot");
            insAlbum.setString(7, ALBUMS[a][2]);
            insAlbum.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            insAlbum.executeUpdate();
            albumCount++;
            System.out.println("seeded: " + title + " (" + photos + " photos, " + ALBUMS[a][1] + ")");
        }
        c.commit();
        c.close();
        System.out.println("done: " + albumCount + " albums, " + picCount + " pictures");
    }

    /** 1024x768 abstract art: themed gradient sky, horizon band, translucent blobs. */
    static BufferedImage makePhoto(Color[] theme, Random rnd) {
        int w = 1024, h = 768;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color top = mix(theme[0], rnd), bottom = mix(theme[1], rnd);
        g.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g.fillRect(0, 0, w, h);
        int horizon = h / 2 + rnd.nextInt(h / 4);
        g.setColor(withAlpha(mix(theme[2], rnd), 180));
        g.fillRect(0, horizon, w, h - horizon);
        for (int i = 0; i < 6 + rnd.nextInt(6); i++) {
            g.setColor(withAlpha(mix(theme[rnd.nextInt(3)], rnd), 40 + rnd.nextInt(90)));
            int r = 30 + rnd.nextInt(220);
            g.fillOval(rnd.nextInt(w) - r / 2, rnd.nextInt(h) - r / 2, r, r);
        }
        g.dispose();
        return img;
    }

    /** Mirrors PictureUtil.createThubnail: 132 wide, proportional height capped at 150. */
    static BufferedImage thumbnail(BufferedImage src) {
        int tw = 132;
        float scale = (float) src.getWidth() / (float) tw;
        int th = Math.round(src.getHeight() / scale);
        if (th > 150) th = 150;
        Image scaled = src.getScaledInstance(tw, th, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return out;
    }

    static byte[] jpeg(BufferedImage img) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", b);
        return b.toByteArray();
    }

    static Color mix(Color base, Random rnd) {
        int j = 36;
        return new Color(
            clamp(base.getRed() + rnd.nextInt(j * 2) - j),
            clamp(base.getGreen() + rnd.nextInt(j * 2) - j),
            clamp(base.getBlue() + rnd.nextInt(j * 2) - j));
    }

    static Color withAlpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }
    static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    static String newId() { return UUID.randomUUID().toString().replace("-", ""); }
}
