/*
 * PictureComment.java
 *
 * Created on January 10, 2006, 4:00 PM
 * POJO for picture comments
 */

package com.ezjcc.picops;

import java.util.Date;

/**
 *
 * @author Jason
 */
public class PictureComment {
    private String id;
    private String picID;
    private Date timestamp;
    private String comment;
    private String userName;
    /**
     * Creates a new instance of PictureComment 
     */
    public PictureComment() {
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getPicID() {
        return picID;
    }
    public void setPicID(String picID){
        this.picID = picID;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getComment() {
        return comment;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
    public String getUserName() {
        return userName;
    }
    public void setTimestamp (Date timestamp)
    {
        this.timestamp = timestamp;
        
    }
    public Date getTimestamp ()
    {
        return timestamp;
    }
    
}
