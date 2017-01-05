/*
 * Activate.java
 *
 * Created on December 5, 2005, 4:21 PM
 *
 * Helper Class with method(s) to complete activation
 */

package com.ezjcc.picops;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Jason
 */
public class Activate {
    
    /**
     * Creates a new instance of Activate
     */
    public static String completeActivation(String id) {
        String status = "";
        Transaction tx1;
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            tx1 = session.beginTransaction(); //start a database transaction
            //see if user is still in the database
            Query q = session.createQuery("Select count(*) from User where lower(id) LIKE lower(:id)");
            q.setParameter("id",id,Hibernate.STRING);
            Integer inDatabase = (Integer)q.list().get(0);
           // tx1.commit();
            if (inDatabase.intValue() == 0) //user has been deleted
            {
                status = "<br>Sorry, User has been purged from database, please signup again";
            }
            else{
           
            User temp = new User();
            session.load(temp, id); //load the user object with the data from the database based on the id key
            //System.out.println("the user is " +temp.getNameFirst()); //debug
            temp.setValidated(true);
            session.update(temp);
            tx1.commit();
            }
        } catch (Exception e){
            e.printStackTrace();
           
            status = "<br>Error commiting information to the database, please try again</br>"; //catch all for problems commiting this object to the database
        } finally{
            HibernateUtil.closeSession();
            return status;
        }
        
    }
    
}
