/*
 * PictureUtil.java
 *
 * 12/2005
 */

package com.ezjcc.picops;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Jason
 */
public class PictureUtil {
    
    /**
     * getImage and byteToImage are addapted from Hibernate Form topic http://fourm.hibernate.org/viewtopic.php?t=957741&highlight=image
     * 
     *This was a test method, but could be used in the future to do a batch add of images that are located as files on the server.  Takes a image path as 
     *input and converts into a byte[], which is the format that the image is stored in in the db
     */
    
    public static byte[] getImageFromFile(String imagePath) {  //this was a test method, leaving incase needed in the future, perhaps for batch uploads?
        try{
            Iterator writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter)writers.next();
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(b);
            writer.setOutput(ios);
            File file = new File(imagePath);
            BufferedImage bi = ImageIO.read(file);
            writer.write(bi);
            return b.toByteArray();
        } catch (Exception e){e.printStackTrace(); return null;}
    }
    
    /**
     * Method to convert a byte array to an image.  The image is stored in the database as binary data and the java Image
     * type is not serializable so we have to convert images to byte arrays to be stored and do the reverse to convert them back to Image types.
     */
    public static BufferedImage byteToImage(byte[] bimg) { //create a buffered image from a byte[]
        try{
            ByteArrayInputStream b = new ByteArrayInputStream(bimg);
            ImageInputStream i = ImageIO.createImageInputStream(b);
            BufferedImage img = ImageIO.read(i);
            return img;
        } catch(Exception e){e.printStackTrace(); return null;}
    }
    /**
     * Handle the upload of files.  Files are uploaded using a multi-part form response.  The doPost method handled getting the form fields and the files into vectors which alonf with the albumid are suppled to this method
     * iterates through the vectors, validates form input, validates file input, calls methods to create thumbnail image, validate image type, check quota, and store the image and thumbnail.
     * This could work for any number of images, although the form limits to 5 at a time for performace reasons.
     */
    public static String handleImageUpload(Vector formFields, Vector files, String albumID)  //called from servlet to do the dirty work of taking the FileItem from the multipart message, validating it, and storing it
    {
        String status = "";
        Iterator iter = files.iterator(); //can handle several file uploads at once
        Iterator iter2 = formFields.iterator();
        String title = "";
        String description = "";
        String photographer = "";
        String date1 = "";
        String date2 = "";
        String date3 = "";
        String date = "";
        while (iter2.hasNext()){
            FileItem item = (FileItem) iter2.next();
            String fieldName = item.getFieldName(); //get the field values from the vector
            if (fieldName.equals("pictureTitle")) {
                title = item.getString();
            }
            if (fieldName.equals("description")) {
                description = item.getString();
            }
            if (fieldName.equals("photographer")) {
                photographer = item.getString();
            }
            if (fieldName.equals("date1")) {
                date1 = item.getString();
            }
            if (fieldName.equals("date2")) {
                date2 = item.getString();
            }
            if (fieldName.equals("date3")) {
                date3 = item.getString();
            }
        }
        //validate the fields
        if (title.contains("<")||title.length()>200) {
            status = "<br><strong>ERROR:</strong> Sorry, title cannot contain HTML or be more than 200 characters";
            return status;
        }
        if (description.contains("<")||description.length()>1000) {
            status = "<br><strong>ERROR:</strong> Sorry, description cannot contain HTML or be more than 1000 characters";
            return status;
        }
        if (photographer.contains("<")||photographer.length()>100) {
            status = "<br><strong>ERROR:</strong> Sorry, photographer cannot contain HTML or be more than 100 characters";
            return status;
        }
        
        if (date1.equals("")==false || date2.equals("")==false || date3.equals("")==false){
            try{
                Integer datep1 = Integer.valueOf(date1);
                Integer datep2 = Integer.valueOf(date2);
                Integer datep3 = Integer.valueOf(date3);
                if (datep1.intValue() >12 || datep2.intValue() > 31) {
                    status = "<br><strong>ERROR:</strong> Sorry, date supplied is not valid";
                    return status;
                }
                date = datep1.toString()+"/"+datep2.toString()+"/"+datep3.toString();
            } catch (Exception e) {status = "<br><strong>ERROR:</strong> Sorry, date supplied is not valid"; return status;}
        }
        //done validation
        while (iter.hasNext()) {
            try{
                FileItem picture = (FileItem)iter.next();
                String fileNamePath = picture.getName(); //gets the complete otiginal path for the uploaded file
                String[] splitFilePath = fileNamePath.split("[\\\\[/]]"); //split the filename into path components based on the path seperator which is either a \ or /
                String fileName = splitFilePath[splitFilePath.length-1]; //this should be the actual filename, which is what we want
                String contentType = picture.getContentType(); //get content type that the client uploaded, can't really trust this though so more validation will be required
                //do some primitive validation of content type here, will do more sophisticated in the getImage method.
                //System.out.println("Received file " + fileName + " of type "+ contentType);
                long pictureSize = picture.getSize();
                if (pictureSize > 3072000) //We are not accepting images larger than 3MB
                {
                    status = "<br><strong>ERROR:</strong> Sorry, your image exceeds the maximum size of 3MB";
                    return status;
                }
                if (pictureSize < 1024) //We are not accepting images smaller than 1KB
                {
                    status = "<br><strong>ERROR:</strong> Sorry, your image did not make it to the server or is too small (Less than 1KB)";
                    return status;
                }
                
                String imageType = getImageType(picture.getInputStream()); //get the InputStream from the FileItem and check it's type by trying to read it as an image
                if (imageType == null) {
                    status = "<br><strong>ERROR:</strong> Sorry, image is not in a valid image format.  Valid formats are .jpg, .gif, .bmp, and .png";
                    return status;
                }
                byte [] pictureInBytes = getImage(picture.getInputStream()); //convert the FileItem to a byte[]
                if (pictureInBytes.length != 0) //looks like we have a valid image, store it
                {
                    //Ok, check to see if this upload will put the user over the storage quota
                    //create a config object.
                    SiteConfig config = SiteConfigUtil.getSiteConfig();
                    Album album = AlbumUtil.getAlbumObject(albumID);
                    long userTotalUsage = AlbumUtil.getStorageUsage(album.getOwnerID());
                    System.out.println((float)userTotalUsage/1048576 + (float)pictureSize/1048576 + " Quota "+ config.getQuota() );
                    if ((float)userTotalUsage/1048576 + (float)pictureSize/1048576 > config.getQuota()) {
                        status = "<br><strong>Error:</strong> You have exceeded the storage quota of "+config.getQuota()+ "MB.  To upload this photo or album, please delete other photos to make space";
                        return status;
                    }else{
                        status = storeImage(pictureInBytes, imageType, fileName, pictureSize, albumID, title, description,date, photographer);//commit this image to the database
                    }
                } else{status = "<br><strong>Error:</strong> Sorry, could not convert your image into a format for storage, please make sure you have a valid image";
                return status;
                }
            } catch (Exception e){
                e.printStackTrace();System.out.println("Problem in handleImageUpload()");
                status = status + "<br><strong>ERROR:</strong> storing image, please try again";
                return status;
            }
        } //end while loop
        return status;
    }
    /**
     * Validate form input, lods pic from db using hibernates, sets new attributes and updates the DB
     */
    public static String handleImageUpdate(String picID, String title, String description, String photographer, String date1, String date2, String date3) {
        String status ="";
        String date = "";
        //validate the fields
        if (title.contains("<")||title.length()>200) {
            status = "<br><strong>ERROR:</strong> Sorry, title cannot contain HTML or be more than 200 characters";
            return status;
        }
        if (description.contains("<")||description.length()>1000) {
            status = "<br><strong>ERROR:</strong> Sorry, description cannot contain HTML or be more than 1000 characters";
            return status;
        }
        if (photographer.contains("<")||photographer.length()>100) {
            status = "<br><strong>ERROR:</strong> Sorry, photographer cannot contain HTML or be more than 100 characters";
            return status;
        }
        
        if (date1.equals("")==false || date2.equals("")==false || date3.equals("")==false){
            try{
                Integer datep1 = Integer.valueOf(date1);
                Integer datep2 = Integer.valueOf(date2);
                Integer datep3 = Integer.valueOf(date3);
                if (datep1.intValue() >12 || datep2.intValue() > 31) {
                    status = "<br><strong>ERROR:</strong> Sorry, date supplied is not valid";
                    return status;
                }
                date = datep1.toString()+"/"+datep2.toString()+"/"+datep3.toString();
            } catch (Exception e) {status = "<br><strong>ERROR:</strong> Sorry, date supplied is not valid"; return status;}
        }
        //done validation
        try{
            Picture pic = new Picture(); //create a new Picture object
            pic.setId(picID);
            //Load the Picture object with data from the database
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction();
            session.load(pic, picID);
            tx1.commit();
            pic.setImageTitle(title);
            pic.setImagePhotographer(photographer);
            pic.setImageDescription(description);
            pic.setImageDate(date);
            Transaction tx2 = session.beginTransaction();
            session.update(pic, picID);
            tx2.commit();
        }catch(Exception e){e.printStackTrace();
        return "<br><strong>ERROR:</strong> Error Updating Picture Information";
        } finally{HibernateUtil.closeSession(); return status;}
    }
    /**
     * Returns the image type of image in an IO stream or null if invalid image format or one that the sun image library does not handle
     */
    public static String getImageType(InputStream imageStream) {
        String imageType;
        try{
            ImageInputStream iis = ImageIO.createImageInputStream(imageStream); //get the input stream into a imageinputstream format.  This will allow us to determine the image type and appropriate readers and writters
            Iterator readers = ImageIO.getImageReaders(iis); //not sure why this needs to be an iterator, but the reader type should be determined based on the structure of the uploaded file
            ImageReader reader = (ImageReader)readers.next(); //get the reader that can read this format, if there is none, this file type is not supported
            imageType = reader.getFormatName(); //get the format of the uploaded file based on the file signiture that was automatically determined
            System.out.println("Image type was "+imageType);
            return imageType;
        } catch (Exception e){e.printStackTrace();return "";}
    }
    /**
     * Returns a byte[] of an InputStream stream by converting into an ImageInputStram and then a bufferedimage and then to a byte[]
     */
    public static byte[] getImage(InputStream imageStream) {  //return a byte[] of the image for storage in the database
        try{
            ImageInputStream iis = ImageIO.createImageInputStream(imageStream);
            Iterator readers = ImageIO.getImageReaders(iis); //not sure why this needs to be an iterator, but the reader type should be determined based on the structure of the uploaded file
            ImageReader reader = (ImageReader)readers.next(); //get the reader that can read this format, if there is none, this file type is not supported
            reader.setInput(iis);
            //Got to go through some stuff to account for non-standard encoded jpegs http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4705399
            ImageReadParam param = reader.getDefaultReadParam();
            ImageTypeSpecifier typeToUse = null;
            for (Iterator i = reader.getImageTypes(0); i.hasNext(); ) {
                ImageTypeSpecifier type =(ImageTypeSpecifier) i.next();
                if (type.getColorModel().getColorSpace().isCS_sRGB()){
                    typeToUse = type;
                }
            }
            if (typeToUse!=null) {
                param.setDestinationType(typeToUse);
                
                ImageWriter writer = ImageIO.getImageWriter(reader); //get an image writer that will write based on the format that the original was in.
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(b);
                writer.setOutput(ios);
                
                //BufferedImage bi = ImageIO.read(iis); //make a BufferedImage object
                BufferedImage bi = reader.read(0, param);
                writer.write(bi); //so, we created a buffered image that is wrote to an imageoutputstream which is wrote to a bytearrayoutputstram which becomes a bytearray
                return b.toByteArray();
            } else return null;
        } catch (Exception e){e.printStackTrace(); System.out.println("Problem in getImage, perhaps image was not in a usable format"); return null; }
    }
     /**
     * Store image in the db by creating a picture object from supplied parameters and storing it and a thumbnail for it (calls createThumbnail) to the DB using hibernate
     */
    public static String storeImage(byte[] img, String imageType, String fileName, long pictureSize, String albumID, String title, String description, String date, String photographer) { //as the name implies, store the image by presisting the picture object to the database
        String status = "";
        try{
            Picture pic = new Picture(); //create the picture object and set its properties
            pic.setAlbumID(albumID);
            pic.setImage(img);
            pic.setUploadDate(new Date());
            pic.setImageType(imageType);
            pic.setFileName(fileName);
            pic.setPictureSize(pictureSize);
            pic.setImageTitle(title);
            pic.setImageDescription(description);
            pic.setImageDate(date);
            pic.setImagePhotographer(photographer);
            Thumbnail tn = new Thumbnail(); //create the thumbnail object and set its properties
            BufferedImage thumbnail = createThubnail(pic);
            if (thumbnail == null) {
                status = "<br><strong>ERROR:</strong>Error creating Thubnail";
                return status; //problem creating the thubnail, don't go any further
            } else{
                ByteArrayOutputStream out = new ByteArrayOutputStream(); //convert thumbnail to a JPEG encoded byte[] first
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); //jpeg encoder outputs to a stream, in this case we use a byte[] because that's how this image will be stored
                encoder.encode(thumbnail);
                tn.setImage(out.toByteArray());
                System.out.println("User Uploaded file of "+img.length+" Bytes ->"+ pic.getImage().length/1024 +" KB -> " + (float)img.length/1048576+ " MB" );
                Session session = HibernateUtil.currentSession(); //get a session
                Transaction tx1 = session.beginTransaction();
                session.save(pic);
                tn.setId(pic.getId());
                session.save(tn);
                tx1.commit();
                // HibernateUtil.closeSession();
                //cleanup (Dealing with memory intensive objects, so need to try to free them up as quickly as posible.  Damn you java garbage collector!)
                status = "ID=" +pic.getId(); //need this info for storing albums with icon
                pic = null;
            }
        } catch (Exception e){e.printStackTrace();status = "<br><strong>ERROR:</strong> Error commiting picture to the database";} finally{return status;}
    }
    /**
     * Outputs an image to the browser.  Scales image if scaleSize is !=0 calling createScaledPic. Otherwise sets the content type the broswer should expect to the correct image type
     * and output the picture to the browser via the HttpServletResponse  
     */
    public static void outputImage(ServletOutputStream out, HttpServletResponse response, String picId, int scaleSize){ //output an image to the browser
        BufferedImage iout;
        Picture pic;
        try{
            pic = new Picture(); //create a new Picture object
            pic.setId(picId);
            //Load the Picture object with data from the database
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction();
            session.load(pic, picId);
            tx1.commit();
            HibernateUtil.closeSession();
            //loaded, now convert to a buffered image and output in throwse correct format.
            //For scaled objects, we will output as a JPEG, otherwise we will encode using the original format...
            
            if (scaleSize != 0) //if a scale size has been specified
            {
                System.out.println("Made it to need to scale");//debug
                iout = PictureUtil.byteToImage(pic.getImage()); //gives us a buffered image from the binary data that was stored in the database
                System.out.println("Got the bufferedImage");//debug
                BufferedImage temp = createScaledPic(iout, scaleSize);
                System.out.println("Made it to output encoder");//debug
                response.setContentType("image/JPEG");
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(temp);
                param.setQuality(0.70f, true);
                encoder.encode(temp, param);
                out.close();
                temp=null; //clean up, not sure if this actually helps free up memory
                encoder = null;
                iout = null;
            } else //output original
            {
                response.setContentType("image/"+pic.getImageType());
                out.write(pic.getImage());
            }
            
            pic = null; //the goal here being to not wait for garbage collection to free resources used by the pic object, since these objects can use a lot of memory! Not sure if this actually does anything to improve resource usage or not
        } catch (Exception e){e.printStackTrace();iout=null;pic=null;}
    }
     /**
     * Load a thumbnail object from the DB using Hibernate, set content type to JPEG, and output send the image to the browser by writting to the HttpServletResponse
     */
    public static void outputThumbnailImage(ServletOutputStream out, HttpServletResponse response, String picId){ //output an image to the browser
        
        Thumbnail thumb;
        try{
            thumb = new Thumbnail(); //create a new Thumbnail object
            thumb.setId(picId);
            //Load the Thumbnail object with data from the database
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction();
            session.load(thumb, picId);
            tx1.commit();
            HibernateUtil.closeSession(); //done this session
            response.setContentType("image/JPEG"); //all thumbnails are encoded as a jpeg for simplicity and performance
            out.write(thumb.getImage()); //write the raw binary data to the output stream
            thumb = null; //the goal here being to not wait for garbage collection to free resources used by the thumbnail object, since these objects can use a lot of memory! Not sure if this actually does anything to improve resource usage or not
        } catch (Exception e){e.printStackTrace();thumb=null;} //output a red X or something
    }
     /**
     * Creates a thumbnail in BufferedImage format for the supplied Picture object. scales so that the width is 132px and the height is proportinaly but less than 150px
     */
    public static BufferedImage createThubnail(Picture pic) //create thubnail image from the image that was submitted
    { //although we could have created thumbnails dynamically, that takes a lot of resources and time, so we are creating them during the picture upload process and storing them in the DB
        try{
            BufferedImage temp = PictureUtil.byteToImage(pic.getImage()); //get the picture stored as binary data to a bufferedimage object so we can manipulate it.
            int thumbnailWidth = 132; //the width we want this picture to be
            float scaleFactor = (float)temp.getWidth()/(float)thumbnailWidth;
            int thumbnailHight = java.lang.Math.round(temp.getHeight()/scaleFactor); //figure out what the scaled height should be
            if (thumbnailHight > 150) {
                thumbnailHight = 150;
            }
            
            //Note, the goal is to scale the image so it will be close to these bounds
            Image thumbNail = temp.getScaledInstance(thumbnailWidth, thumbnailHight, 1); //this should give us a scaled image with the same aspect ratio
            //Unfortunatly, getScaledInstance returns an Image object.  We will need a BufferedImage object to create a JPEG, so we must go through some steps to convert it
            BufferedImage bufferedImageOut = new BufferedImage(thumbNail.getWidth(null),thumbNail.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics g  = bufferedImageOut.getGraphics();
            g.drawImage(thumbNail, 0,0,null);
            return bufferedImageOut;
        } catch (Exception e){e.printStackTrace();return null;}
    }
     /**
     * returns a BufferedImage from the supplied BufferedImage and the new width (scale) that is proportiontly scalled to <=scale we really only want to scale the image if it's larger than the scale amount, otherwise we will distort the image.
     */
    public static BufferedImage createScaledPic(BufferedImage pic, int scale) //create thubnail image from the image that was submitted
    {
        Image img;
        try{
            
            int imgWidth = scale; //scale is actually the new width - this is something that we could have as a config option at some point
            //Note, the goal is to scale the image so it will be close to these bounds
            float scaleFactor = (float)pic.getWidth()/(float)scale;
            int imgHight = java.lang.Math.round(pic.getHeight()/scaleFactor);
            
            if(pic.getWidth()>scale) //we really only want to scale the image if it's larger than the scale amount, otherwise we will distort the image.
            {
                img = pic.getScaledInstance(imgWidth, imgHight, 1); //this should give us a scaled image with the same aspect ratio
                
                // System.out.println("Got the scaled Image");//debug
                //Unfortunatly, getScaledInstance returns an Image object.  We will need a BufferedImage object to create a JPEG, so we must go through some steps to convert it
                //BufferedImage bufferedImageOut = new BufferedImage(img.getWidth(null),img.getHeight(null), BufferedImage.TYPE_INT_RGB);
                BufferedImage bufferedImageOut = new BufferedImage(imgWidth,imgHight, BufferedImage.TYPE_INT_RGB);
                //// System.out.println("Created new bi");//debug
                // System.out.println("ready to draw");//debug
                Graphics g  = bufferedImageOut.getGraphics();
                // System.out.println("got graphics");//debug
                g.drawImage(img, 0,0,null);
                //  System.out.println("done drawing");//debug
                g.dispose();
                // System.out.println("done disposing");//debug
                img = null; //do some clean up
                pic = null;
                return bufferedImageOut;
            } else {return pic;}
        } catch (Exception e){e.printStackTrace();pic=null; img=null; return null;}
    }
     /**
     * Delete pic from the database. Due to cascade deletes on the database, other dependent rows (such as thumbnails) will also be deleted.
     */
    public static String handleDeletePic(String picID) {
        String status = "";
        try{
            Picture p = new Picture();
            p.setId(picID);
            p.setAlbumID("0");
            p.setImage(new byte[0]);
            p.setImageType("0");
            p.setPictureSize(0);
            p.setUploadDate(new Date());
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.delete(p);
            tx1.commit();
        } catch (Exception e){e.printStackTrace(); status = "<br><strong>ERROR:</strong>Error Deleting Picture.  Picture may have already been deleted";} finally{HibernateUtil.closeSession(); return status;}
    }
     /**
     * Load a picture object from the DB using Hibernateminus the minus image file
     */
    public static Picture getPictureObjectNoImage(String picID){  //using this when we need to update an image
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            Query q = session.createQuery("Select p.albumID, p.imageType,p.uploadDate,p.imageTitle,p.fileName,p.pictureSize, p.imageDescription, p.imagePhotographer, p.imageDate from Picture p where p.id LIKE :picID");
            q.setParameter("picID",picID,Hibernate.STRING);
            
            Iterator it = q.iterate();
            Object[] row = (Object[])it.next();
            Picture pic = new Picture();
            pic.setId(picID);
            pic.setAlbumID((String)row[0]);
            pic.setImageTitle((String)row[3]);
            pic.setUploadDate((Date)row[2]);
            pic.setImageType((String)row[1]);
            pic.setFileName((String)row[4]);
            pic.setPictureSize(((Long)row[5]).longValue());
            pic.setImageDescription((String)row[6]);
            pic.setImagePhotographer((String)row[7]);
            pic.setImageDate((String)row[8]);
            tx1.commit();
            return pic;
        } catch (Exception e){e.printStackTrace();return null;} finally {HibernateUtil.closeSession();}
    }
     /**
     * Validate comment and store it to the DB.  Comments are asociated with a specific picture
     */
    public static String addComment(String picID, String userName, String comment) {
        String status = "";
        if (picID == null ||picID.length()>32) {
            status = "<br><strong>ERROR:</strong>Error, picID not valid";
            return status;
        }
        if (userName.length()>50 ||userName.contains(">")) {
            status = "<br><strong>ERROR:</strong>User Name too long for comment or has HTML.  It can only be 50 chars";
            return status;
        }
        if (comment == null || comment.length()>1000 ||comment.contains(">")) {
            status = "<br><strong>ERROR:</strong>Error, comment cannot be empty or greater than 1000 chars or has HTML";
            return status;
        }
        try{
            PictureComment c = new PictureComment();
            c.setPicID(picID);
            c.setUserName(userName);
            c.setComment(comment);
            c.setTimestamp(new Date());
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.save(c);
            tx1.commit();
        } catch (Exception e){
            e.printStackTrace();
            status = "<br><strong>ERROR:</strong>Error Adding Comment, picture may have been deleted or database error";
        } finally{HibernateUtil.closeSession(); return status;}
        
    }
     /**
     * Get all the comment objects from the DB for a particular picture and return in  a vector
     */
    public static Vector getComments(String picID) {
        Vector comments = new Vector();
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            
            Query q = session.createQuery("Select c.id, c.picID, c.comment, c.timestamp, c.userName from PictureComment c where c.picID LIKE :picID");
            q.setParameter("picID",picID,Hibernate.STRING);
            
            for (Iterator it = q.iterate(); it.hasNext();){
                Object[] row = (Object[])it.next();
                PictureComment c = new PictureComment(); //create comment objects for each row returned and store in vector
                c.setId((String)row[0]);
                c.setPicID((String)row[1]);
                c.setComment((String)row[2]);
                c.setTimestamp((Date)row[3]);
                c.setUserName((String)row[4]);
                comments.add(c);
            }
            tx1.commit();
            return comments;
        } catch (Exception e){e.printStackTrace();return null;} finally {HibernateUtil.closeSession();}
    }
     /**
     * Delete a comment from the DB
     */
    public static String deleteComment(String commentID) {
        String status = "";
        try{
            Session session = HibernateUtil.currentSession(); //get a session
            PictureComment c = new PictureComment();
            Transaction tx1 = session.beginTransaction(); //start a database transaction
            session.load(c, commentID);
            session.delete(c); //delete  the comment
            tx1.commit();
        } catch (Exception e){e.printStackTrace();return"<br /> <strong>Error:</Strong> Error, could not delete comment";} finally {HibernateUtil.closeSession(); return status;}
        
    }
} //end of class PictureUtil
