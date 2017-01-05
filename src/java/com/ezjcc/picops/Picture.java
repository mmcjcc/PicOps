/*
 * Picture.java
 *
 * 2005
 * This class is a POJO  (Plain Old Java Object) which will be used to represent an image
 *
 */

package com.ezjcc.picops;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class Picture {
    private String id;
    private String albumID;
    private byte[] image;
    private Date uploadDate;
    private String imageType;
    private String imageTitle;
    private String imagePhotographer;
    private String imageDate;
    private String imageDescription;
    private String fileName;
    private long pictureSize;
    
    /**
     * POJO for a Picture
     */
    public Picture() {
    }
    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getAlbumID() {
        return albumID;
    }
    public void setAlbumID(String albumID){
        this.albumID = albumID;
    }
    public String getImageType() {
        return imageType;
    }
    public void setImageType(String imageType){
        this.imageType = imageType;
    }
    public void setUploadDate(Date date){
        this.uploadDate = date;
    }
    public Date getUploadDate() {
        return uploadDate;
    }
    
    public void setImage(byte[] img){
        this.image = img;
    }
    public byte[] getImage() {
        return image;
    }
    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }
    public String getImageTitle() {
        return imageTitle;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }
    public void setPictureSize(long pictureSize) {
        this.pictureSize = pictureSize;
    }
    public long getPictureSize() {
        return pictureSize;
    }
    public void setImagePhotographer(String photographer) {
        this.imagePhotographer = photographer;
    }
    public String getImagePhotographer() {
        return imagePhotographer;
    }
    public void setImageDescription(String description) {
        this.imageDescription = description;
    }
    public String getImageDescription() {
        return imageDescription;
    }
    public void setImageDate(String imageDate) {
        this.imageDate = imageDate;
    }
    public String getImageDate() {
        return imageDate;
    }
    
}
