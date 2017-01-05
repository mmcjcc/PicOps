/*
 * AlbumUtil.java
 *
 * Created on December 2005
 *
 *
 */

package com.ezjcc.picops;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.fileupload.FileItem;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Jason
 */
public class AlbumUtil {
    
    /**
     * Test to see if the album is marked as a public album.  This is used when determining if an album should be displayed to a user that is not logged in.
     * @param albumID 
     * @return boolean
     */
    public static boolean isPubAlbum(String albumID) {
        boolean publicAlbum = false;
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select count(*) from Album where id LIKE :albumID and albumVisibility LIKE 'public'");
            q.setParameter("albumID",albumID,Hibernate.STRING); //the idea here is that if the result is 0, the album was not public
            Integer numAlbums2 = (Integer)(q.list().get(0));
            tx1.commit();
            if (numAlbums2.intValue()>0) {
                publicAlbum = true;
            }
            return publicAlbum;
        } catch (Exception e){e.printStackTrace();  return publicAlbum;} //some problem talking to the database.  The -1 shows will tell us this.
        finally {HibernateUtil.closeSession();}
    }
    /**
     * Get number of albums for a particular user.  Uses Hibernate query to count albums where the user id matches.
     */
    public static int getNumAlbums(String userID){ //Get the number of albums a particular user has
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select count(*) from Album where ownerid LIKE :userID");
            q.setParameter("userID",userID,Hibernate.STRING);
            Integer numAlbums2 = (Integer)(q.list().get(0));
            tx1.commit();
            return numAlbums2.intValue();
        } catch (Exception e){e.printStackTrace();  return -1;} //some problem talking to the database.  The -1 shows will tell us this.
        finally {HibernateUtil.closeSession();}
    } //end get num albums
    
    /**
     * Get the amount of public albums for a particular user by making a Hibernate query to count albums that are marked as public for that user.
     */
    public static int getNumPublicAlbums(String userID){ //Get the number of public albums a particular user has
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select count(*) from Album where ownerid LIKE :userID and albumVisibility LIKE 'public'");
            q.setParameter("userID",userID,Hibernate.STRING);
            Integer numAlbums2 = (Integer)(q.list().get(0));
            tx1.commit();
            return numAlbums2.intValue();
        } catch (Exception e){e.printStackTrace();  return -1;} //some problem talking to the database.  The -1 shows will tell us this.
        finally {HibernateUtil.closeSession();}
    } //end get num albums
    
    /**
     * Get a vector of album objects for a particular user by making a Hibernate query to select the id's of all albums owned by the user
     * and them use the Hibernate load method to create album objects based on the id.  These objects are put in a vector and returned.
     */
    public static Vector getAlbums(String userID){ //will return a vector of album objects
        Vector albums = new Vector();
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select id from Album where ownerid LIKE :userID");
            q.setParameter("userID",userID,Hibernate.STRING);
            Iterator it = q.iterate();
            while (it.hasNext()){
                String albumID = (String)it.next();
                
                Album album = new Album(); //make an album object
                session.load(album, albumID); //load album object with database data
                albums.add(album);
                tx1.commit();
            }
            
        } catch (Exception e){e.printStackTrace();} finally {HibernateUtil.closeSession();}
        return albums;
    }//end get albums
     /**
     * Get a vector of album objects for a particular user that are marked as public by making a Hibernate query to select the id's of all albums owned by the user
     * and them use the Hibernate load method to create album objects based on the id.  These objects are put in a vector and returned.
     */
    public static Vector getPubAlbumsForUser(String userID){ //will return a vector of public album objects for the user
        Vector albums = new Vector();
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select id from Album where ownerid LIKE :userID and albumVisibility LIKE 'public'");
            q.setParameter("userID",userID,Hibernate.STRING);
            Iterator it = q.iterate();
            while (it.hasNext()){
                String albumID = (String)it.next();
                Album album = new Album(); //make an album object
                session.load(album, albumID); //load album object with database data
                albums.add(album);
                tx1.commit();
            }
            
        } catch (Exception e){e.printStackTrace();} finally {HibernateUtil.closeSession();}
        return albums;
    }//end get albums
     /**
     * Get a vector of album objects of the size specified.  This returns a vector of random public albums.  Used to display random albums on home page 
     * 
     */
    public static Vector getPubAlbums(int amount){ //will return a vector of size amount of public album objects
        Vector albums = new Vector();
        System.out.println("running get pub albums");
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select id from Album where albumVisibility LIKE 'public'ORDER BY RANDOM()");
            q.setMaxResults(amount); //the random function is specific to Postgres, so this would have to change if porting to another database
            Iterator it = q.iterate();
            
            while (it.hasNext()){
                String albumID = (String)it.next();
                
                Album album = new Album(); //make an album object
                session.load(album, albumID); //load album object with database data using the album id we got from the procesing query
                albums.add(album);
                tx1.commit();
            }
            
        } catch (Exception e){e.printStackTrace();} finally {HibernateUtil.closeSession();}
        return albums;
    }
    /**
     * Get a vector of all album objects that are public
     */
    public static Vector getPubAlbums(){ //will return a vector of size amount of public album objects
        Vector albums = new Vector();
        
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select id from Album where albumVisibility LIKE 'public'");
            Iterator it = q.iterate();
            while (it.hasNext()){
                String albumID = (String)it.next();
                
                Album album = new Album(); //make an album object
                session.load(album, albumID); //load album object with database data
                albums.add(album);
                tx1.commit();
            }
            
        } catch (Exception e){e.printStackTrace();} finally {HibernateUtil.closeSession();}
        return albums;
    }//end getpub albums
    /**
     * Handles album search.  Takes keywords as input. Gets an iterator of all album id's marked public and then loads each one and searches it's description, photographer, and title to see if they contain any of the keywords
     */
    public static Vector getPubAlbumsSearch(String keywords){ //will return a vector of size amount of public album objects
        Vector albums = new Vector();
        keywords = keywords.toLowerCase();
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select id from Album where albumVisibility LIKE 'public'");
            Iterator it = q.iterate();
            while (it.hasNext()){
                String albumID = (String)it.next();
                
                Album album = new Album(); //make an album object
                session.load(album, albumID); //load album object with database data
                if (album.getDescription().toLowerCase().contains(keywords)|| album.getPhotographer().toLowerCase().contains(keywords)|| album.getTitle().toLowerCase().contains(keywords)) {
                    albums.add(album);
                }
                tx1.commit();
            }
            
        } catch (Exception e){e.printStackTrace();} finally {HibernateUtil.closeSession();}
        return albums;
    }//end getpub albums search
    /**
     * Get a vector of photo objects (minus the data for the image) for a particular album.  Make a hibernate query to select everything but the image file
     */
    public static Vector getAlbumContents(String albumID) {
        Vector albumContents = new Vector();
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select p.id, p.imageType,p.uploadDate,p.imageTitle,p.fileName,p.pictureSize, p.imageDescription, p.imagePhotographer, p.imageDate from Picture p where p.albumID LIKE :albumID");
            q.setParameter("albumID",albumID,Hibernate.STRING);
            //we selected everything but the actual image and are storing in image objects which we put in the vector
            for (Iterator it = q.iterate(); it.hasNext();){
                Object[] row = (Object[])it.next();
                Picture pic = new Picture();
                pic.setAlbumID(albumID);
                pic.setId((String)row[0]);
                pic.setImageTitle((String)row[3]);
                pic.setUploadDate((Date)row[2]);
                pic.setImageType((String)row[1]);
                pic.setFileName((String)row[4]);
                pic.setPictureSize(((Long)row[5]).longValue());
                pic.setImageDescription((String)row[6]);
                pic.setImagePhotographer((String)row[7]);
                pic.setImageDate((String)row[8]);
                albumContents.add(pic);
            }
            tx1.commit();
        } catch (Exception e){e.printStackTrace();} finally {HibernateUtil.closeSession();}
        return albumContents;
    }
    /**
     * Get the size (in KB) of all the images in a particular album.  We recorded the image size into the database when the photo was uploaded,
     * so we simply select the picture size from each picture in the album using hibernate and iterate through the results, adding this to a vector as a Long.
     * this method is used by getStorageUsage() to get the storage figures for all albums
     */
    public static Vector getPictureSizes(String albumID) { //get the picture sizes for all pictures in an album
        Vector pictureSizes = new Vector();
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select p.pictureSize from Picture p where p.albumID LIKE :albumID");
            q.setParameter("albumID",albumID,Hibernate.STRING);
            //we selected everything but the actual image and are storing in image objects which we put in the vector
            for (Iterator it = q.iterate(); it.hasNext();){
                
                pictureSizes.add((Long)it.next());
            }
            tx1.commit();
        } catch (Exception e){e.printStackTrace();} finally {HibernateUtil.closeSession();}
        return pictureSizes;
    }
    /**
     * Handle the upload of albums.  Takes a vector of fields (the input fields from the form), files (there will only be one file in the vector), and the owner id
     * Validates the input to the form, stores the album (calling methods to store and create the album ico thumbnail), and returns a status. If status is null, is is good.
     */
    public static String handleAlbumUpload(Vector fields, Vector files, String ownerID){
        String status = "";
        Iterator iter = fields.iterator();
        String title = "";
        String description = "";
        String iconID = "";
        String photographer = "";
        String albumVisibility = "";
        while (iter.hasNext()){
            FileItem item = (FileItem) iter.next();
            String fieldName = item.getFieldName(); //get the field values from the vector
            if (fieldName.equals("title")) {
                title = item.getString();
            }
            if (fieldName.equals("description")) {
                description = item.getString();
            }
            if (fieldName.equals("photographer")) {
                photographer = item.getString();
            }
            if (fieldName.equals("albumVisibility")) {
                albumVisibility = item.getString();
            }
        }
        if (title == "" || title.length()>200) //album title is required
        {
            status = "<br><strong>ERROR:</strong> Valid title is required, no HTML tags allowed";
            return status;
        }
        if (description.contains("<")|| description.length()>1000) //No html allowed
        {
            status = "<br><strong>ERROR:</strong> No HTML tags allowed in description and cannot be more than 1000 characters";
            return status;
        }
        if (files.size()>1) //should never have more than one file in album upload
        {
            status = "<br><strong>ERROR:</strong> File parse error, try again";
            return status;
        }
        if (albumVisibility == "" || (albumVisibility.equals("public")!=true && albumVisibility.equals("private") !=true)) {
            status = "<br><strong>ERROR:</strong> Album needs to be public or private";
            return status;
        }
        if (photographer.contains("<")|| photographer.length()>100) //No html allowed
        {
            status = "<br><strong>ERROR:</strong> No HTML tags allowed in photographer";
            return status;
        }
        //create an album object
        Album album = new Album();
        album.setOwnerID(ownerID);
        album.setTitle(title);
        album.setDescription(description);
        album.setAlbumVisibility(albumVisibility);
        album.setPhotographer(photographer);
        album.setCreationDate(new Date());
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.save(album);
            tx1.commit();
            
            Vector imageFields = new Vector();
            String iconStoreResults = PictureUtil.handleImageUpload(imageFields, files,album.getId() ); //this handles storing the icon as both a picture and a thumbnail
            Transaction tx2 = HibernateUtil.currentSession().beginTransaction();
            
            if (iconStoreResults.contains("ID=")) //image was stored sucessfully
            {
                iconID = iconStoreResults.substring(3); //this strips off the ID= part that is returned from PictureUtil
                album.setIcon(iconID);
                HibernateUtil.currentSession().update(album); //now that we have the icon ID, update the album
                tx2.commit();
                
            } else{ //error storing icon image
                status = "<br><strong>ERROR:</strong> Error storing album, please try again"+ iconStoreResults;
                HibernateUtil.currentSession().delete(album); //problem with storing image, roll back transaction
                tx2.commit();
            }
            
        } catch (Exception e){e.printStackTrace();status = status +  "<br><strong>ERROR:</strong> Error storing album, please try again"; }
        
        finally {HibernateUtil.closeSession(); return status;}  //close session, return status
    }
    /**
     * Handles album deletes by creating an album object using the supplied id and then deleting it from the db using Hibernate;s session.delete() function.
     */
    public static String handleAlbumDelete(String albumID) {
        String status = "";
        try{
            Album temp = new Album();
            temp.setId(albumID);
            temp.setOwnerID("1");
            temp.setTitle("1");
            temp.setAlbumVisibility("1");
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.delete(temp);
            tx1.commit();
            return status;
        } catch (Exception e){e.printStackTrace(); return "<br /><strong>ERROR:</strong>Error Deleting Album"; } finally {HibernateUtil.closeSession();}
        
    }
    /**
     * Return an Album object given an ID.  Uses hibernate's session.load() to load the info from the db into an album object
     */
    public static Album getAlbumObject(String albumID) {
        try{
            Album temp = new Album();
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.load(temp, albumID);
            tx1.commit();
            return temp;
            
        } catch (Exception e) {return null;} finally{HibernateUtil.closeSession();}
    }
    public static String updateAlbumIcon(String albumID, String newIconID) {
        String status = "";
        try{
            Album temp = new Album();
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.load(temp, albumID);
            temp.setIcon(newIconID);
            session.update(temp);
            tx1.commit();
            return status;
            
        } catch (Exception e) {
            status = "<br><strong>ERROR:</strong> Error changing album icon, album may have been removed or database error";
            e.printStackTrace();
            return status;
        } finally{HibernateUtil.closeSession();}
    }
    /**
     * Validates form input, loads album from db and updates album to db
     */
    public static String updateAlbumData(String albumID, String title, String description, String photographer, String albumVisibility) {
        String status = "";
        if (title == "" || title.length()>200) //album title is required
        {
            status = "<br><strong>ERROR:</strong> Valid title is required, no HTML tags allowed";
            return status;
        }
        if (description.contains("<")|| description.length()>1000) //No html allowed
        {
            status = "<br><strong>ERROR:</strong> No HTML tags allowed in description and cannot be more than 1000 characters";
            return status;
        }
        
        if (albumVisibility == "" || (albumVisibility.equals("public")!=true && albumVisibility.equals("private") !=true)) {
            status = "<br><strong>ERROR:</strong> Album needs to be public or private";
            return status;
        }
        if (photographer.contains("<")|| photographer.length()>100) //No html allowed
        {
            status = "<br><strong>ERROR:</strong> No HTML tags allowed in photographer";
            return status;
        }
        try{
            Album temp = new Album();
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.load(temp, albumID);
            temp.setTitle(title);
            temp.setDescription(description);
            temp.setPhotographer(photographer);
            temp.setAlbumVisibility(albumVisibility);
            session.update(temp);
            tx1.commit();
            return status;
            
        } catch (Exception e) {
            status = "<br><strong>ERROR:</strong> Error changing album data, album may have been removed or database error";
            e.printStackTrace();
            return status;
        } finally{HibernateUtil.closeSession();}
    }
    /**
     * Get the total storage for a particular user.  Gets all albums for a particular user, gets the total for each album from getPictureSizes(), 
     * then adds up each album and add albums together.
     */
    public static long getStorageUsage(String userID) {
        
        Vector albums = getAlbums(userID);
        long total = 0;
        for (int i=0; i<albums.size();i++) {
            Album temp = (Album)albums.elementAt(i);
            Vector sizes = getPictureSizes(temp.getId());
            long totalForAlbum=0;
            for (int j=0;j<sizes.size();j++) {
                totalForAlbum = totalForAlbum + ((Long)sizes.elementAt(j)).longValue();
            }
            
            total = total + totalForAlbum;
        }
        
        return total;
    }
}
