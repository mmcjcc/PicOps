/*
 * HibernateUtil.java
 *
 * Created on November 26, 2005, 8:06 PM
 *
 * Class Source: http://www.hibernate.org/42.html#A11
 * Will be used to start Hibernate by building a SessionFactory.
 */

package com.ezjcc.picops;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.cfg.*;

public class HibernateUtil {
    
    private static Log log = LogFactory.getLog(HibernateUtil.class);
    
    private static final SessionFactory sessionFactory;
    
    static {
        try {
            // Create the SessionFactory
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static final ThreadLocal session = new ThreadLocal();
    
    public static Session currentSession() throws HibernateException {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }
    
    public static void closeSession() throws HibernateException {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
            s.close();
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
}