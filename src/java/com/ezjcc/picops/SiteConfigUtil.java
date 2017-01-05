/*
 * SiteConfigUtil.java
 *
 * Created on January 12, 2006, 5:41 PM
 *
 * 
 */

package com.ezjcc.picops;

import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Jason
 */
public class SiteConfigUtil {
    /**
     * returns a SiteConfig object based on site config data in DB using Hibernate
     */
  public static SiteConfig getSiteConfig ()
    {
       SiteConfig config = new SiteConfig();;
        //config.setId(0);
        String configID = "0";
        Transaction tx1;
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            tx1 = session.beginTransaction(); //start a database transaction
            session.load(config, configID);
            tx1.commit();
            return config;
           
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("<br>Error getting siteconfig</br>"); //catch all for problems commiting this object to the database
            return null;
        } finally{
            HibernateUtil.closeSession();
            return config;
        }
        
    }
    
}
