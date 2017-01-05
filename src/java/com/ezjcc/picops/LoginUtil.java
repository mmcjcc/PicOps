/*
 * LoginUtil.java
 *
 * Created on December 27, 2005, 2:23 PM
 *
 *
 */

package com.ezjcc.picops;

import java.util.Iterator;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Administrator
 */
public class LoginUtil {
    /**
     * Handle login.  Takes username and password, does some initial validation of the input, then selects the password stored in the db, encryts the supplied password and compares to the encrypted password in the database
     * Note: passwords are encrypted using SHA encrypt function in the UserUtil class
     */
    public static String[] handleLogin(String userName, String password){
        String [] status = new String[2];
        status[0] = "";
        //we know the user name and password have to conform to these specifications, so check before bothering with the database.
        if (userName == "" || userName.length() > 20) {
            status[0] = status[0] + "<br><strong>ERROR:</strong> User Name supplied is not valid";
            return status; //don't need to go any further
        }
        
        if (password == "" || password.length() > 20 || password.length() < 6) {
            status[0] = status[0] + "<br><strong>ERROR:</strong>Password supplied is not valid";
            return status; //don't need to go any further
        }
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select password, validated, id from User where lower(username) LIKE lower(:userName)");
            q.setParameter("userName",userName,Hibernate.STRING);
            String passwordFromDB = "";
            Boolean validated = Boolean.valueOf(false);
            String userID = "";
            try{
            Iterator it = q.iterate();
            Object[] row = (Object[])it.next();
            passwordFromDB = (String)row[0];
            validated = (Boolean)row[1];
            userID = (String)row[2];
            } //if we have an exception, this means no results were returned, hence invalid username
            catch (Exception e){status[0] = status[0] + "<br><strong>ERROR:</strong>Username or Password not valid";return status;}
            tx1.commit();
            HibernateUtil.closeSession();
            
            if (passwordFromDB != "") {
                if (UserUtil.encrypt(password).equals(passwordFromDB)) { //password matched
                    if (validated.booleanValue() != false)
                    {
                    status[0] = "passed";
                    status[1] = userID;
                    }
                    else { //user did not activate account yet!
                        status[0] = "<br><strong>ERROR:</strong> You have not activated your account yet.  Please check your email and click the link to activate your account.";
                    }
                    return status;
                } else {
                    //wrong password
                    status[0] = status[0] + "<br><strong>ERROR:</strong>Username or Password not valid";
                    return status;
                }
            } else { //Username not in database
                status[0] = status[0] + "<br><strong>ERROR:</strong>Username or Password not valid";
                return status;
            }
        } catch (Exception e){e.printStackTrace(); status[0]="<br><strong>ERROR:</strong> Error fetching from database";
        return status;
        }
    }
    /**
     *  Uses Hibernate to retrieve user info from the database into a user object.
     */
    public static User getUserObject (String userID)
    {
        User temp = new User();
        Transaction tx1;
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            tx1 = session.beginTransaction(); //start a database transaction
            session.load(temp, userID); //load the user object with the data from the database based on the id key
            tx1.commit();
            return temp;
           
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("<br>Error commiting information to the database, please try again</br>"); //catch all for problems commiting this object to the database
            return temp;
        } finally{
            HibernateUtil.closeSession();
            return temp;
        }
        
    }
    
}

