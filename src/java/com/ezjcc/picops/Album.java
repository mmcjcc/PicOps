/*
 * Album.java
 *
 * Created on December, 2005, 5:18 PM
 *
 * POJO for an album
 */

package com.ezjcc.picops;

import java.util.Date;

/**
 *
 * @author Administrator
 */
public class Album {
    private Date creationDate;
    private String id;
    private String ownerID;
    private String title;
    private String icon;
    private String description;
    private String albumVisibility;
    private String photographer;
    
    /** Creates a new instance of Album */
    public Album() {
    }
    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public void setOwnerID(String ownerID){
        this.ownerID = ownerID;
    }
    
    public String getOwnerID() {
        return ownerID;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon){
        this.icon = icon;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getPhotographer() {
        return photographer;
    }
    
    public void setPhotographer(String photographer){
        this.photographer = photographer;
    }
    
    public String getAlbumVisibility() {
        return albumVisibility;
    }
    public void setAlbumVisibility(String albumVisibility){
        this.albumVisibility = albumVisibility;
    }
    
    
    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate){
        this.creationDate = creationDate;
    }
    
} //end album class
