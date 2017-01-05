/*
 * Thumbnail.java
 *
 * Created on December 19, 2005, 2:54 PM
 *
 * POJO to hold a Thubnail object
 */

package com.ezjcc.picops;

/**
 *
 * @author Administrator
 */
public class Thumbnail {
    private String id;
    private byte[] image;
    /** Creates a new instance of Thumbnail */
    public Thumbnail() {
    }
    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    } public void setImage(byte[] img){
        this.image = img;
    }
    public byte[] getImage() {
        return image;
    }
}
