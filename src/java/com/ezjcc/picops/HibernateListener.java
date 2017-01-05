/*
 * HibernateListner.java
 *
 * Created on November 26, 2005, 8:19 PM
 *
 * Source: http://www.hibernate.org/114.html
 * This listener initializes and closes Hibernate on deployment and undeployment, 
 * instead of the first user request hitting the application.  Need to add reference to this in your web.xml
 */
package com.ezjcc.picops;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class HibernateListener implements ServletContextListener {
     /**
     * Ensures that db connection is active before first user hits app. 
     */
    public void contextInitialized(ServletContextEvent event) {
        HibernateUtil.getSessionFactory(); // Just call the static initializer of that class    
    }

    public void contextDestroyed(ServletContextEvent event) {
        HibernateUtil.getSessionFactory().close(); // Free all resources
    }
}
