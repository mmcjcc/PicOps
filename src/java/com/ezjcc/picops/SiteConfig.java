/*
 * SiteConfig.java
 *
 * Created on January 12, 2006, 5:19 PM
 *
 * This will hold configuration info for the site such as what mail server to use
 * This could be expanded to add some other features later
 */

/**
 * @author Jason
 */
package com.ezjcc.picops;

public class SiteConfig {
   private String id;
   private String mailServer;
   private String webServer;
   private String emailAddress;
   private int quota;
   
    /** Creates a new instance of SiteConfig */
    public SiteConfig() {
    }
     public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    
    public String getMailServer() {
        return mailServer;
    }
    public void setMailServer (String mailServer){
        this.mailServer = mailServer;
    }
     public String getWebServer() {
        return webServer;
    }
    public void setWebServer(String webServer){
        this.webServer = webServer;
    }
     public String getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }
    public int getQuota()
    {
        return quota;
    }
    public void setQuota(int quota)
    {
        this.quota = quota;
    }
}
