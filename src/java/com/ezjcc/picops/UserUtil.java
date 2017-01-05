/*
 * UserUtil.java
 *
 * Created on November 27, 2005
 *
 * This class contains helper methods for use during new user signup.  These methods are contained in a seperate class for organizational and abstraction purpose.
 */

package com.ezjcc.picops;

/**
 *
 * @author Jason Cohen
 */
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Iterator;
import javax.mail.internet.AddressException; //using the java mail api for fuctions to assist with email address validation
import javax.mail.internet.InternetAddress;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserUtil {
    
    /** This method is used to do some basic checking to see if the user passed a "valid" email address in that we will check to make sure it is formed with a name a @ and a domain 
    *
    * This method is addapted from http://www.javapractices.com/Topic180.cjp 
    */
    public static boolean isValidEmailAddress(String aEmailAddress){
        if (aEmailAddress == null) return false;
        
        boolean result = true;
        try { //try to create an InternetAddress object with the email address that was passed
            InternetAddress emailAddr = new InternetAddress(aEmailAddress);
            emailAddr.validate(); //does some built in syntax validation of the address
            if ( ! hasNameAndDomain(aEmailAddress) ) { //check format some more and test domain name to see if it is valid
                result = false;
            }
        } catch (AddressException ex){
            result = false; //could not create an InternetAddress with the passed address so returning false
        }
        return result;
    }
    /**
     * Figures out if a given string is broken down into format name @ domain . xxx
     */
    private static boolean hasNameAndDomain(String aEmailAddress){
        String[] tokens = aEmailAddress.split("@"); //doing some basic checking to see if there is there is an @ sign and that there is a dot in the domain section
        if (tokens.length ==2 && tokens[0] != "" && tokens[1].contains(".")) {
            try{
                InetAddress domain = InetAddress.getByName(tokens[1]);
                if (domain.getHostAddress() == null) //this tries a dns lookup of the hostname part of the address to see if it can be resolved
                {
                    return false;
                } else return true; //looks like we have a valid host name
            } catch (Exception e){return false;} //an exception occured when trying to create the InetAddress object, return false
            
        } else return false ;
    } //end hasNameAndDomain
    
    /**
     * Will use this to encrypt passwords and answers using one-way encryption (SHA Algorithm).  
     * Based on example at http://www.devarticles.com/c/a/Java/Password-Encryption-Rationale-and-Java-Example
     */
    public static String encrypt(String text) {
        try //need to put this in a try/catch block incase SHA is not avalible
        {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] bytes = md.digest(text.getBytes());
            String hash = (new sun.misc.BASE64Encoder()).encode(bytes); //convert the sha output to base64 encoded text
            return hash;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    } //end encrypt
    /**
     * Handles user registration.  Validates form input (checking lengths, HTML tags, and if the username is already taken), encrypts the password, creates user object, stores user object using Hibernate, and sends validation email to user.
     * Account is not active until user clicks on link in the email 
     */
    public static String handleRegistration(String nameFirst, String nameLast, String userName, String email, String password, String password2, int question, String answer) {
        String status = ""; //will use this to return any errors or any other status info
        //First step is to validate info
        if (nameFirst == "" || nameFirst.length() > 20 || nameFirst.contains("<")) //I don't want the user to be able to inject html into the input for security purposes (hence the search for "<")
        {
            status = "<br><strong>ERROR:</strong>Valid First Name is Required";
        }
        if (nameLast == "" || nameLast.length() > 20 || nameLast.contains("<")) {
            status = status + "<br><strong>ERROR:</strong>: Valid Last Name is Required";
        }
        if (userName == "" || userName.length() > 20 || userName.contains("<")) {
            status = status + "<br><strong>ERROR:</strong> Valid User Name is Required";
        }
        if (email == "" || email.length() > 50 || email.contains("<") || isValidEmailAddress(email)==false ) //calling method to check the email address as defined above
        {
            status = status + "<br><strong>ERROR:</strong>Valid email is Required, your email address could not be validated.  If this is a valid email address, please try again as we may be having DNS problems";
        }
        if (password == "" || password.length() > 20 || password.length() < 6) {
            status = status + "<br><strong>ERROR:</strong>Valid Password is Required.  Must be greater than 6 characters";
        }
        if (password2.equals(password) == false) {
            status = status + "<br><strong>ERROR:</strong> Passwords do not match";
        }
        if (answer == "" || answer.length() > 50 || answer.contains("<")) {
            status = status + "<br><strong>ERROR:</strong>An answer to the secret question is Required and cannot contain HTML markup such as < ";
        }
        //validation is complete, next create a user object and commit it to the database
        if (status == "") {
            User temp = new User();
            
            try{
                Session session = HibernateUtil.currentSession(); //get a session
                Transaction tx1 = session.beginTransaction(); //start a database transaction
                Query q = session.createQuery("Select count(*) from User where lower(username) LIKE lower(:userName)");
                q.setParameter("userName",userName,Hibernate.STRING);
                Integer userNameTaken = (Integer)q.list().get(0);
                //Integer userNameTaken = (Integer)session.createQuery("Select count(*) from User where lower(username) LIKE lower('"+userName+"')").list().get(0); //This case-insensitive query will check to see if there are any other users with this username already in the database
                tx1.commit();
                // System.out.println("usernametake ="+userNameTaken);
                if (userNameTaken.intValue() !=0) //this user name already is in use
                {
                    status = status + "<br><strong>ERROR:</strong> Username that you selected is already taken, please choose another one";
                } else{ //user name is free, continue with adding user
                    
                    Transaction tx = session.beginTransaction(); //transaction to store the user
                    temp.setNameFirst(nameFirst);
                    temp.setNameLast(nameLast);
                    temp.setUserName(userName);
                    temp.setEmail(email);
                    temp.setPassword(encrypt(password));
                    temp.setQuestion(question);
                    temp.setAnswer(encrypt(answer.toLowerCase())); //making the secret question answer all lowercase for easier comparison
                    temp.setValidated(false);
                    temp.setRegistrationdate(new Date());
                    session.save(temp);
                    tx.commit();
                    SiteConfig config =SiteConfigUtil.getSiteConfig();
                    System.out.println("Stored User with ID "+temp.getId());
                    String message = "<html><body><p><b>Hello "+nameFirst+" and welcome to PicOps (Online Photo System)!</b><p>There's one last thing you need to do before " +
                            "you can start enjoying PicOps's COOL features, like posting photo albums and sharing them with your friends.  " +
                            "Please click on the link below within the next 48 hours to activate your account.<p><a href='"+config.getWebServer()+"/PicOps/PicOps?Action=activate&id="+temp.getId()+"'>Click Here to Activate!</a></body></html>";
                    status = MailUtil.sendMail(config.getMailServer(), email, config.getEmailAddress(), "Welcome to PicWeb!", message);
                    
                    if (status != "")
                    {
                        session.delete(temp);
                    }
                   
                }
            } catch (Exception e){
                e.printStackTrace();
                status = "<br><strong>Error commiting information to the database, please try again</strong></br>"; //catch all for problems commiting this object to the database
            } finally{
                
                HibernateUtil.closeSession();
                return status;
            }
        }
        return status;
    }
    /**
     * Handles password changes.  Validates new password, compares against old, updates the supplied user object and stores to the DB.
     */
    public static String changePassword(String oldpassword, String newpassword, String newpassword2, User user){
        String status = "";
        if (newpassword == "" || newpassword.length() > 20 || newpassword.length() < 6) {
            status = status + "<br><strong>ERROR:</strong>Valid Password is Required.  Must be greater than 6 characters";
        }
        if (newpassword.equals(newpassword2) == false) {
            status = status + "<br><strong>ERROR:</strong> Passwords do not match";
        }
        if (encrypt(oldpassword).equals(user.getPassword()) == false) {
            status = status + "<br><strong>ERROR:</strong> Your old password is not correct!  Please enter the password that you used to login with.";
        }
        if (status == "") {
            try{
                Session session = HibernateUtil.currentSession(); //get a session
                Transaction tx = session.beginTransaction(); //start a database transaction
                user.setPassword(encrypt(newpassword));
                session.update(user); //update the user entry in the database with the new password
                tx.commit();
                HibernateUtil.closeSession();
                return status;
            } catch (Exception e){e.printStackTrace();status= "<br><strong>ERROR:</strong> Error updating password in database, try again"; return status;}
        } else {
            return status;
        }
    }
    /**
     * Sets a new password for user if they can remember their secret question
     */
    public static String forgotPassword(String userName, int questionNum, String answer, String newpassword, String newpassword2){
        String status = "";
        String userID ="";
        if (userName == "" || userName.length() > 20) { //do some basic validation
            status = status + "<br><strong>ERROR:</strong> User Name supplied is not valid";
            return status; //don't need to go any further
        }
        
        if (newpassword == "" || newpassword.length() > 20 || newpassword.length() < 6) {
            status = status + "<br><strong>ERROR:</strong>Password supplied is not valid";
            return status; //don't need to go any further
        }
        if (newpassword.equals(newpassword2) == false) {
            status = status + "<br><strong>ERROR:</strong> Passwords do not match";
            return status; //don't need to go any further
        }
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select id from User where lower(username) LIKE lower(:userName)"); //get the id of the user from the database
            q.setParameter("userName",userName,Hibernate.STRING);
            userID = "";
            try{
                userID = (String) q.list().get(0);
                
            } //if we have an exception, this means no results were returned, hence invalid username
            
            catch (Exception e){status = status + "<br><strong>ERROR:</strong>Username or Secret Question not valid";return status;}
            tx1.commit();
            //System.out.println("user id is "+userID); /debug
            if (userID !="") {
                Transaction tx2 = session.beginTransaction();
                User temp = new User();
                session.load(temp, userID);
                //ok, we have the user from the database in a user object, now see if the question was valid
                if ((temp.getQuestion() == questionNum) && (temp.getAnswer().equals(encrypt(answer))) ){
                    temp.setPassword(encrypt(newpassword));
                    session.update(temp);
                    tx2.commit();
                    HibernateUtil.closeSession();
                    return status; //updated password, complete
                } else{ //question and/or answer did not match
                    status = status + "<br><strong>ERROR:</strong>Username or Secret Question not valid";
                    HibernateUtil.closeSession();
                    return status;
                }
            }else{ //userID was null
                status = status + "<br><strong>ERROR:</strong>Username or Secret Question not valid";
                HibernateUtil.closeSession();
                return status;
            }
            
        } catch (Exception e){e.printStackTrace();status =status + "<br><strong>ERROR:</strong> Error working with database"; HibernateUtil.closeSession(); return status;} finally {HibernateUtil.closeSession();return status;}
    }
    /**
     * Gets the user id for a supplied name.  Used in display public albums link for user
     */
    public static String getUserID(String userName) {
        String userID ="";
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select id from User where lower(username) LIKE lower(:userName)"); //get the id of the user from the database
            q.setParameter("userName",userName,Hibernate.STRING);
            userID = "";
            try{
                userID = (String) q.list().get(0);
                
            } //if we have an exception, this means no results were returned, hence invalid username
            
            catch (Exception e){return null;}
            tx1.commit();
            
        }catch (Exception e){e.printStackTrace();return null;} finally{HibernateUtil.closeSession();return userID;}
        
    }
   /**
     * Get username of user for a supplied userID
     */
    public static String getUserName(String userID) {
        String userName ="";
        
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select userName from User where id LIKE :id"); //get the id of the user from the database
            q.setParameter("id",userID,Hibernate.STRING);
            
            try{
                userName = (String) q.list().get(0);
                
            } //if we have an exception, this means no results were returned, hence invalid id
            
            catch (Exception e){return null;}
            tx1.commit();
            
        }catch (Exception e){e.printStackTrace();return null;} finally{HibernateUtil.closeSession();return userName;}
        
    }
} //end class


