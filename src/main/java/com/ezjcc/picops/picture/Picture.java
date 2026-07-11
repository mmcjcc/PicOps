package com.ezjcc.picops.picture;

import com.ezjcc.picops.album.Album;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pictures")
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(nullable = false)
    private byte[] data;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "taken_at")
    private Instant takenAt;

    @Column(length = 120)
    private String camera;

    @Column(name = "gps_lat")
    private Double gpsLat;

    @Column(name = "gps_lon")
    private Double gpsLon;

    private Short orientation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String meta;

    /** Metadata-stripped, orientation-applied variant served to non-owners. */
    @Column(name = "clean_data")
    private byte[] cleanData;

    @Column(name = "faces_scanned", nullable = false)
    private boolean facesScanned;

    protected Picture() {
    }

    public Picture(Album album, byte[] data, String contentType, String fileName, long sizeBytes) {
        this.album = album;
        this.data = data;
        this.contentType = contentType;
        this.fileName = fileName;
        this.sizeBytes = sizeBytes;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Album getAlbum() { return album; }
    public String getContentType() { return contentType; }
    public String getFileName() { return fileName; }
    public long getSizeBytes() { return sizeBytes; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }

    public Instant getTakenAt() { return takenAt; }
    public String getCamera() { return camera; }
    public Double getGpsLat() { return gpsLat; }
    public Double getGpsLon() { return gpsLon; }
    public byte[] getData() { return data; }
    public byte[] getCleanData() { return cleanData; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTakenAt(Instant takenAt) { this.takenAt = takenAt; }
    public void setCamera(String camera) { this.camera = camera; }
    public void setGpsLat(Double gpsLat) { this.gpsLat = gpsLat; }
    public void setGpsLon(Double gpsLon) { this.gpsLon = gpsLon; }
    public void setOrientation(Short orientation) { this.orientation = orientation; }
    public void setMeta(String meta) { this.meta = meta; }
    public void setCleanData(byte[] cleanData) { this.cleanData = cleanData; }
}
