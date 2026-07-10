package com.ezjcc.picops.picture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "thumbnails")
public class Thumbnail {

    @Id
    @Column(name = "picture_id")
    private UUID pictureId;

    @Column(nullable = false)
    private byte[] data;

    protected Thumbnail() {
    }

    public Thumbnail(UUID pictureId, byte[] data) {
        this.pictureId = pictureId;
        this.data = data;
    }

    public UUID getPictureId() { return pictureId; }
    public byte[] getData() { return data; }
}
