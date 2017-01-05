/*
 * MailUtil.java
 *
 * Created on November 29, 2005, 1:14 AM
 *
 * Helper class for sending email.  Addapted from example at: http://www.javaworld.com/javaworld/jw-10-2001/jw-1026-javamail.html
 * NOTE: Need java activation framework (activation.jar) and javamail jars for this to work.  
 */

package com.ezjcc.picops;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Jason
 */
public class MailUtil {
    /**
     * Handles sending email messages using the javamail package
     */
    public static String sendMail(String smtpServer, String to, String from, String subject, String body) {
        String status = "";
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpServer); //javamail needs this info in a Properties object to send smtp mail
            Session session = Session.getInstance(props, null); //start a new mail session
            // -- Create a new message --
            Message msg = new MimeMessage(session);
            // -- Set the FROM and TO fields --
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));
            // -- Set the subject and body text --
            msg.setSubject(subject);
            msg.setContent(body, "text/html");
            // -- Set some other header information --
            msg.setHeader("X-Mailer", "PicOps");
             msg.setSentDate(new Date());
            // -- Send the message --
            Transport.send(msg);
            return status;
        } catch (Exception ex) {
            System.out.println("Error sending mail to "+ to);
            status = "<br><strong>ERROR:</strong>Error sending email, please try again later";
            ex.printStackTrace();
             return status;
        }
    }
}




