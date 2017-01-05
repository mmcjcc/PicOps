package com.ezjcc.picops;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Jason Cohen
 *
 */
//Note, we will be using Hibernate to manage the database activities
public class PicOps extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try { //putting this in a try/catch block to keep the user from seeing any errors if they occur
            //  System.out.println("doing get"); //debug
            
            if (request.getParameter("Action")==null && (request.getSession().getAttribute("page")!=null && (String)(request.getSession().getAttribute("page")).toString() =="welcome")) { //first thing that happens when a user hits the site, set the pag to welcome
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "welcome"); //store a session var that will be used by the jsp view to know what to display
                Vector pubAlbums = AlbumUtil.getPubAlbums(3); //these albums are displayed on welcome page as links
                request.setAttribute("pubAlbums", pubAlbums);
                dispatch.forward(request, response);
                
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("login")) {
                System.out.println("login action");
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "login"); //store a session var that will be used by the jsp view to know what to display
                dispatch.forward(request, response);
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("logout")) { //handle logout
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                HttpSession session = request.getSession();
                session.invalidate(); //destroy session and all assosiated data
                request.getSession().setAttribute("page", "welcome");
                Vector pubAlbums = AlbumUtil.getPubAlbums(3);
                request.setAttribute("pubAlbums", pubAlbums);
                dispatch.forward(request, response); //send user back to welcome page
                
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("newuser")) { //tell jsp to display new user signup form
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "newusersignup"); //store a session var that will be used by the jsp view to know what to display
                dispatch.forward(request, response);
                
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("addalbum")) { //tell jsp to display addalbum form
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "addalbum"); //store a session var that will be used by the jsp view to know what to display
                dispatch.forward(request, response);
                
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("deletealbum")) { //tell jsp to display addalbum form
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "albums"); //store a session var that will be used by the jsp view to know what to display
                String status = "";
                String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                if (request.getSession().getAttribute("userBean")!=null) { //test to see if this is a logged in user and if this album belongs to him
                    User userBean = (User)request.getSession().getAttribute("userBean");
                    Album album = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                    if (album.getOwnerID().equals(userBean.getId())) {
                        userIsAlbumOwner = "true";
                    }
                }
                request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                if ((String)request.getSession().getAttribute("albumID")!=null &&(String)request.getSession().getAttribute("loggedIn")!=null && userIsAlbumOwner.equals("true")) {
                    status = AlbumUtil.handleAlbumDelete((String)request.getSession().getAttribute("albumID"));
                    if (status == "") {
                        status = "<br><strong>Album deleted successfully</strong>";
                    }
                } else {
                    status = "<br><strong>Error:</strong>Error deleting album";
                }
                System.out.println("delete :"+status);
                request.setAttribute("error", status);
                dispatch.forward(request, response);
                
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("passwordchange")) {
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "changepassword"); //store a session var that will be used by the jsp view to know what to display
                dispatch.forward(request, response);
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("forgotpassword")) {
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "forgotpassword"); //store a session var that will be used by the jsp view to know what to display
                dispatch.forward(request, response);
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("activate")) { //handle activation
                //System.out.println("running action=activate");
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                String userId = request.getParameter("id");
                String result = Activate.completeActivation(userId);
                System.out.println("Activation result "+result);
                if (result != "") //check to see if an error message was returned
                {
                    //return user to signup page with error message(s)
                    request.getSession().setAttribute("page", "newusersignup"); //store a session var that will be used by the jsp view to know what to display
                    request.setAttribute("error", result); //pass the error message along as part of the request
                } else {
                    request.getSession().setAttribute("page", "login"); //store a session var that will be used by the jsp view to know what to display
                    request.setAttribute("activated", "true"); //pass the error message along as part of the request
                    //return user to home page
                }
                dispatch.forward(request, response); //passes off to the jsp for display
            }//end if action = activate
            else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("displayalbums")) {
                String error = "";
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "displayalbums");
                SiteConfig config =SiteConfigUtil.getSiteConfig();
                request.setAttribute("webaddress", config.getWebServer());
                Integer quota = new Integer(config.getQuota()); //can't put primitive type in request
                request.setAttribute("quota", quota);
                User userBean = (User)request.getSession().getAttribute("userBean"); //get the user object from the session
                int numAlbums = AlbumUtil.getNumAlbums(userBean.getId()); //see how many albums this user has
                if (numAlbums == -1) //we had a problem with the DB
                {
                    error = "<br><strong>Error:</strong> Error getting information from the database, please try again</br>";
                    request.setAttribute("error", error);
                    dispatch.forward(request, response); //done
                } else {
                    Integer numAlbums2 = new Integer(numAlbums); //can't put an int into the request attribute
                    request.setAttribute("numAlbums", numAlbums2);
                    if (numAlbums >0) {
                        //alright, there be some albums, lets get them into a vector of albums
                        Vector albums = AlbumUtil.getAlbums(userBean.getId());
                        //make sure this worked
                        if (albums.isEmpty()==false) //we have some album objects
                        {
                            request.setAttribute("albumVector", albums);
                            long usage = AlbumUtil.getStorageUsage(userBean.getId());
                            request.setAttribute("storageUsage", new Long(usage));
                            dispatch.forward(request, response); //send the info on the way to the jsp
                        } else { //no album object for whatever reason
                            error = "<br><strong>Error:</strong>Error getting albums from the database, please try again</br>";
                            request.setAttribute("error", error);
                            dispatch.forward(request, response); //done
                        }
                    } else{
                        dispatch.forward(request, response); //0 albums, forward request with num albums only}
                    }
                    
                }
                
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("displaypubalbums") && request.getParameter("user")!=null) { //handle request for a user's public albums
                String error = "";
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "displaypubalbums");
                String userName = request.getParameter("user");
                String userID = UserUtil.getUserID(userName);
                request.setAttribute("userName", userName);
                if (userID == null) {
                    error = "<br><strong>ERROR:</strong> Error finding user in database.  Invalid user or database error";
                    request.setAttribute("error", error);
                    dispatch.forward(request, response); //done
                } else{
                    
                    int numAlbums = AlbumUtil.getNumPublicAlbums(userID); //see how many albums this user has
                    if (numAlbums == -1) //we had a problem with the DB
                    {
                        error = "<br><strong>Error:</strong> Error getting information from the database, please try again</br>";
                        request.setAttribute("error", error);
                        dispatch.forward(request, response); //done
                    } else {
                        Integer numAlbums2 = new Integer(numAlbums); //can't put an int into the request attribute
                        request.setAttribute("numAlbums", numAlbums2);
                        if (numAlbums >0) {
                            //alright, there be some public albums, lets get them into a vector of albums
                            Vector albums = AlbumUtil.getPubAlbumsForUser(userID);
                            //make sure this worked
                            if (albums.isEmpty()==false) //we have some album objects
                            {
                                request.setAttribute("albumVector", albums);
                                dispatch.forward(request, response); //send the info on the way to the jsp
                            } else { //no album object for whatever reason
                                error = "<br><strong>Error:</strong> Error getting albums from the database, please try again</br>";
                                request.setAttribute("error", error);
                                dispatch.forward(request, response); //done
                            }
                        } else{
                            dispatch.forward(request, response); //0 albums, forward request with num albums only}
                        }
                    }
                }
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("displayalbum")) { //handle the display of a particular album
                String error = "";
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "displayalbum");
                request.getSession().setAttribute("albumID", request.getParameter("id").trim()); //we'll need this for adding pics and such to the album
                String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                if (request.getSession().getAttribute("userBean")!=null) { //test to see if this is a logged in user and if this album belongs to him
                    User userBean = (User)request.getSession().getAttribute("userBean");
                    Album album = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                    if (album.getOwnerID().equals(userBean.getId())) {
                        userIsAlbumOwner = "true";
                    }
                }
                request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                
                if ( request.getParameter("id")==null) //no id specified
                {
                    error = "<br><strong>Error:</strong>Error, no album ID specified";
                    request.setAttribute("error", error);
                    dispatch.forward(request, response);
                }
                if ( request.getParameter("showinfo")!=null) //no id specified
                {
                    request.getSession().setAttribute("showinfo",request.getParameter("showinfo") );
                    
                }else {
                    if (request.getSession().getAttribute("showinfo")==null){
                        request.getSession().setAttribute("showinfo","yes" );
                    }
                }
                if (userIsAlbumOwner.equals("true")) {  //this user's album, handle
                    String albumID = request.getParameter("id").trim();
                    Vector albumContents = AlbumUtil.getAlbumContents(request.getParameter("id").trim());
                    if (albumContents.size()==0) //did not retrieve any photos
                    {
                        error = "<br><strong>Error:</strong>Error, no photos in this album or database error";
                        request.setAttribute("error", error);
                        dispatch.forward(request, response);
                    } else{
                        Album album = AlbumUtil.getAlbumObject(request.getParameter("id").trim());
                        request.setAttribute("album", album);
                        request.getSession().setAttribute("albumIcon", album.getIcon());
                        request.getSession().setAttribute("albumContents", albumContents); //have a vector of photo objects( minus actual image) putting this in the session so we can use it again in thumbnail or sldeshow view without re-fetching it
                    }
                    dispatch.forward(request, response);
                } else{ //the user is NOT the owner of this album, it is public perhaps
                    if (AlbumUtil.isPubAlbum(request.getParameter("id").trim())==true) {
                        Vector albumContents = AlbumUtil.getAlbumContents(request.getParameter("id").trim());
                        if (albumContents.size()==0) //did not retrieve any photos
                        {
                            error = "<br><strong>Error:</strong>Error, no photos in this album or database error";
                            request.setAttribute("error", error);
                            dispatch.forward(request, response);
                        } else{
                            Album album = AlbumUtil.getAlbumObject(request.getParameter("id").trim());
                            request.setAttribute("album", album);
                            request.getSession().setAttribute("albumIcon", album.getIcon());
                            request.getSession().setAttribute("albumContents", albumContents); //have a vector of photo objects( minus actual image) putting this in the session so we can use it again in thumbnail or sldeshow view without re-fetching it
                            String userName = UserUtil.getUserName(album.getOwnerID());
                            request.setAttribute("albumOwnerName", userName);
                            dispatch.forward(request, response);
                        }
                        
                    } else { //album was not public, anymore at least
                        error = "<br><strong>Error:</strong>Error, album not public or database error";
                        request.setAttribute("error", error);
                        dispatch.forward(request, response);
                    }
                }
            }else  if (request.getParameter("Action")!=null && request.getParameter("Action").equals("displaypic") && request.getParameter("id")!=null) { //handles request to send an image to the browser
                //System.out.println("Displaying a pic part of doGet"); //debug
                String picId = request.getParameter("id").trim(); //get the picture id into a string
                int scaleSize=0;
                if (request.getParameter("size")!=null) {
                    scaleSize = Integer.valueOf(request.getParameter("size").trim()).intValue(); //this may be null, tells us if we need to scale the pic and if so, how much.  Since we are proportionaly scaling the image, we only need the width
                }
                ServletOutputStream out = response.getOutputStream(); //allows for binary output.
                if (scaleSize <2000){
                    PictureUtil.outputImage(out, response, picId, scaleSize);
                }
                
            } else  if (request.getParameter("Action")!=null && request.getParameter("Action").equals("deletepic")){
                String status = "";
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "slideshow");
                String picID = "";
                if (request.getParameter("id")!=null) { //delete requested from thubnail view
                    picID = request.getParameter("id");
                } else{ //delete requested from slideshow view
                    picID = (String)request.getSession().getAttribute("picID");
                }
                Album temp = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                User userBean = (User)request.getSession().getAttribute("userBean");
                if (userBean.getId().equals(temp.getOwnerID())==false) {
                    status = "<br><strong>Error:</strong>Error, you cannot delete this picture because it does not belong to you";
                    request.setAttribute("error", status);
                    dispatch.forward(request, response);
                } else if (temp.getIcon().equals(picID)) {
                    status = "<br><strong>Error:</strong>Error, you cannot delete this picture because it is the album icon";
                    request.setAttribute("error", status);
                    dispatch.forward(request, response);
                } else {
                    status = PictureUtil.handleDeletePic(picID);
                    request.setAttribute("error", status);
                    dispatch.forward(request, response);
                }
            } //end pic delete
            else if ((request.getParameter("Action")!=null && request.getParameter("Action").equals("displayslideshow"))||(request.getParameter("Action")!=null && request.getParameter("Action").equals("addcomment")||request.getAttribute("deletedComment")!=null)) { //handles request to send an image to the browser
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "displayslideshow");
                String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                
                if (request.getSession().getAttribute("userBean")!=null) {
                    User userBean = (User)request.getSession().getAttribute("userBean");
                    Album album = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                    if (album.getOwnerID().equals(userBean.getId())) {
                        userIsAlbumOwner = "true";
                    }
                }
                request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                
                Vector albumContents = AlbumUtil.getAlbumContents((String)request.getSession().getAttribute("albumID")); //get a fresh vector of the album contents
                Integer imagePos;
                if(albumContents == null) //maybe album has been deleted or something
                {
                    request.setAttribute("error", "<br><strong>Error:</strong>Error, album may bave been deleted or database error.  Go back to view album page to check");
                    dispatch.forward(request, response);
                } else{
                    if (request.getParameter("id")!=null) {
                        try{
                            imagePos = Integer.valueOf(request.getParameter("id")); //we passed what position we want to start at.  This is the position of the image in the original album array
                        } catch (Exception e){imagePos = Integer.valueOf(0);request.getSession().setAttribute("page", "album");} //we deleted this picture from thubnail view
                    } else{
                        imagePos = Integer.valueOf(0);
                    }
                    if (imagePos.intValue()==albumContents.size()) {
                        imagePos = Integer.valueOf(0); //we were at the end of the album, go to the begining
                    } else if (imagePos.intValue()== -1) {
                        imagePos = Integer.valueOf(albumContents.size()-1); //we were at the begining of the album, go to the end
                    }
                    Picture p = (Picture)albumContents.elementAt(imagePos.intValue());
                    Vector comments = PictureUtil.getComments(p.getId());
                    String imageID = p.getId();
                    System.out.println(imageID);
                    request.setAttribute("imagePos",imagePos);
                    request.setAttribute("image", p);
                    request.setAttribute("albumContents", albumContents); //this is needed to display the filmstrip under the image (ripped off idea from coppermine :-)
                    request.setAttribute("albumSize", new Integer(albumContents.size()));
                    if (comments.size()!=0) {
                        request.setAttribute("comments", comments);
                    }
                    dispatch.forward(request, response);
                }
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("changeicon")) { //handles request to change an icon
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "album");
                String albumID = (String)request.getSession().getAttribute("albumID");
                String newIconID = request.getParameter("id");
                String error = "";
                String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                if (request.getSession().getAttribute("userBean")!=null) { //test to see if this is a logged in user and if this album belongs to him
                    User userBean = (User)request.getSession().getAttribute("userBean");
                    Album album = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                    if (album.getOwnerID().equals(userBean.getId())) {
                        userIsAlbumOwner = "true";
                    }
                }
                request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                
                if ( request.getParameter("id")==null) //no id specified
                {
                    error = "<br><strong>Error:</strong>Error, no album ID specified";
                    request.setAttribute("error", error);
                    dispatch.forward(request, response);
                }
                
                if (albumID.equals("") || newIconID.equals("")) {
                    request.setAttribute("error", "<br><strong>ERROR:</strong> Error changing album icon");
                    dispatch.forward(request, response);
                }
                if (userIsAlbumOwner.equals("false")) {
                    request.setAttribute("error", "<br><strong>ERROR:</strong> You are not the owner of this album!");
                    dispatch.forward(request, response);
                }
                
                else { //all input is good, go for update
                    String status = AlbumUtil.updateAlbumIcon(albumID, newIconID);
                    if (status.equals("")==false) {
                        request.setAttribute("error", status);
                    }
                    dispatch.forward(request, response);
                }
                
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("addpic")) { //handles request to send an image to the browser
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.setAttribute("multiple", request.getParameter("multiple"));
                request.getSession().setAttribute("page", "addpic");
                dispatch.forward(request, response);
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("updatepic")) { //handles request to update image data
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "updatepic");
                String picID = request.getParameter("id").trim();
                Picture p = PictureUtil.getPictureObjectNoImage(picID); //get the picture object
                request.setAttribute("image", p); //put the picture object into the request, jsp will use this to populate fields
                dispatch.forward(request, response);
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("updatealbum")) { //handles request to update image data
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "updatealbum");
                String albumID = request.getParameter("id").trim();
                Album album = AlbumUtil.getAlbumObject(albumID);
                request.setAttribute("album", album); //put the album object into the request, jsp will use this to populate fields
                dispatch.forward(request, response);
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("search") && request.getParameter("pos")!=null){
             doPost(request, response);   
            }
            else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("search")) { //handles request to update image data
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "search");
                dispatch.forward(request, response);
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("deletecomment")&&request.getAttribute("deletedComment")==null) { //handles request to delete comment
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "displayslideshow");
                String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                if (request.getSession().getAttribute("userBean")!=null) { //test to see if this is a logged in user and if this album belongs to him
                    User userBean = (User)request.getSession().getAttribute("userBean");
                    Album album = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                    if (album.getOwnerID().equals(userBean.getId())) {
                        userIsAlbumOwner = "true";
                    }
                }
                request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                if (userIsAlbumOwner.equals("true")) {
                    String status = PictureUtil.deleteComment(request.getParameter("cid"));
                    request.setAttribute("error", status);
                    request.setAttribute("deletedComment", "true"); //this will tell one of the previous else if statements to display the slideshow again.
                }else{
                    request.setAttribute("error", "<br/><strong>Error:</strong> Error, you must be album owner to delete comments");
                }
                doGet(request, response); //need to display the slideshow again, so do the get method
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("displaythumbnail") && request.getParameter("id")!=null) {
                String picId = request.getParameter("id").trim(); //get the picture id into a string
                if (picId.length()==32){  //do a little input validation to try to prevent someone from putting bogus stuff here
                    ServletOutputStream out = response.getOutputStream(); //allows for binary output.
                    PictureUtil.outputThumbnailImage(out, response, picId);
                }
            }//end if action = displaythumbnail
            
            else if (request.getSession().getAttribute("page")==null) { //first thing that happens when a user hits the site, set the pag to welcome
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "welcome"); //store a session var that will be used by the jsp view to know what to display
                Vector pubAlbums = AlbumUtil.getPubAlbums(3);
                request.setAttribute("pubAlbums", pubAlbums);
                dispatch.forward(request, response);
                
            } else {
                //send user to home page
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                request.getSession().setAttribute("page", "welcome"); //store a session var that will be used by the jsp view to know what to display
                Vector pubAlbums = AlbumUtil.getPubAlbums(3);
                request.setAttribute("pubAlbums", pubAlbums);
                dispatch.forward(request, response);
            }
            
        }catch (Exception e){ //some error occured, send user back to welcome page
            e.printStackTrace();
            ServletContext context = this.getServletContext();
            RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
            request.getSession().setAttribute("page", "welcome"); //store a session var that will be used by the jsp view to know what to display
            dispatch.forward(request, response);
        }
    }
    
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        System.out.println("Doing post"); //debug
        
        try { //putting this in a try/catch block to keep the user from seeing any errors if they occur
            boolean isMultipart = FileUpload.isMultipartContent(request); //a multipart request contains a file
            /*Handle User signup request action*/
            if (request.getParameter("Action")!=null && request.getParameter("Action").equals("signup")) {
                System.out.println("running action=signup"); //debug
                //get all of the post vars from the form and trim any blank space off the end
                String nameFirst = request.getParameter("nameFirst").trim();
                String nameLast = request.getParameter("nameLast").trim();
                String userName = request.getParameter("userName").trim();
                String email = request.getParameter("email").trim();
                String password = request.getParameter("password").trim();
                String password2 = request.getParameter("password2").trim();
                int question = Integer.valueOf(request.getParameter("question")).intValue(); //convert to an int
                String answer = request.getParameter("answer").trim().toLowerCase();
                //process the signup
                String result = UserUtil.handleRegistration(nameFirst, nameLast, userName , email , password , password2 , question , answer);
                System.out.println("User Signup Result: "+result);
                if (result.equals("")==false) //check to see if an error message was returned
                {
                    //return user to signup page with error message(s)
                    ServletContext context = this.getServletContext();
                    RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                    request.getSession().setAttribute("page", "newusersignup"); //store a session var that will be used by the jsp view to know what to display
                    request.setAttribute("error", result); //pass the error message along as part of the request
                    dispatch.forward(request, response);
                } else {
                    //return user to home page with status message to check email to finish signup
                    ServletContext context = this.getServletContext();
                    RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                    request.getSession().setAttribute("page", "emailsent"); //store a session var that will be used by the jsp view to know what to display
                    request.setAttribute("status", "emailSent"); //let the jsp know that an email was sent
                    dispatch.forward(request, response);
                }
            } //end if action = signup
            
            else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("login")) { //handle logins
                System.out.println("doing login");
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                String userName = request.getParameter("userName").trim();
                String password = request.getParameter("password").trim();
                String[] loginStatus = LoginUtil.handleLogin(userName,password); //this gives me the status of the login in [0] and the user id in [1]
                //System.out.println(loginStatus);
                
                if (loginStatus[0] == "passed") {
                    request.getSession().setAttribute("page", "albums");
                    request.getSession().setAttribute("loggedIn", "true");
                    User userBean = LoginUtil.getUserObject(loginStatus[1]);
                    request.getSession().setAttribute("userBean", userBean); //store the user object in the session so we can access properties of it from the jsp
                    dispatch.forward(request, response);
                    
                } else{
                    request.setAttribute("error", loginStatus[0]);
                    request.getSession().setAttribute("page", "login");
                    dispatch.forward(request, response);
                }
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("changepassword")) { //handle password changes
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                String oldpassword = request.getParameter("oldpassword").trim();
                String newpassword = request.getParameter("newpassword").trim();
                String newpassword2 = request.getParameter("newpassword2").trim();
                User userBean = (User)(request.getSession().getAttribute("userBean")); //get the user object out of the session data
                String changeStatus = UserUtil.changePassword(oldpassword, newpassword, newpassword2, userBean);
                if (changeStatus == "") { //password change was good
                    userBean.setPassword(UserUtil.encrypt(newpassword)); //update the user bean that we are maintaining in the session
                    request.getSession().setAttribute("page", "changepassword");
                    request.getSession().setAttribute("userBean", userBean);
                    request.getSession().setAttribute("error", "Password was successfully updated");
                } else { //password change failed
                    request.getSession().setAttribute("page", "changepassword");
                    request.getSession().setAttribute("error", changeStatus);
                }
                dispatch.forward(request, response);
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("forgot")) { //handle forgot password
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                String userName = request.getParameter("userName").trim();
                int question = Integer.valueOf(request.getParameter("question")).intValue(); //convert to an int
                String answer = request.getParameter("answer").trim().toLowerCase();
                String newpassword = request.getParameter("newpassword").trim();
                String newpassword2 = request.getParameter("newpassword2").trim();
                String status = UserUtil.forgotPassword(userName, question, answer, newpassword, newpassword2);
                
                if (status == "") { //password change was good
                    request.getSession().setAttribute("page", "forgotpassword");
                    request.setAttribute("error", "Password was successfully changed");
                } else { //password change failed
                    request.getSession().setAttribute("page", "forgotpassword");
                    request.getSession().setAttribute("error", status);
                }
                dispatch.forward(request, response);
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("displayalbums")){
                doGet(request, response); //this handles the time when the user logs on and are redirected to the displayalbums page.  This is due to the fact that the previous request was a post, so it's forwarded as a post, I suppose.
                
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("addalbum")){
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
                if (isMultipart == true){
                    System.out.println("multipart upload");
                    
             /* Servlets have no native support for parsing a multipart post request that conform to RFC 1867, so we
              * are using a package from Jakarta to do this called Comons FileUpload.  Some of the following code is adapted from an example at jakarta.apache.org/commons/fileupload/using.html */
                    String status = ""; //need this to hold the result of the image upload and storage process. A empty status will mean everything is good
                    Vector formFields = new Vector(); //we'll store the form text items (not the picture) in this to send to the method that will process the actual storage of the image
                    Vector files = new Vector(); //store files from the request object
                    DiskFileUpload upload = new DiskFileUpload();
                    //set the max request size that we will accept.  This information comes from the config table
                    /* NOTE TO SELF, fix this later with config table */
                    upload.setSizeMax(4000000); //set to 4 megs for max upload size we will even look at (data and form fields).
                    
                    try{
                        List uploadItems = upload.parseRequest(request); //Parse the request into a list
                        //go through the list and handle the picture files and form items
                        Iterator iter = uploadItems.iterator();
                        while (iter.hasNext()){
                            FileItem item = (FileItem) iter.next(); //cast the List items that we obtained by parsing the request into file items
                            
                            if (item.isFormField()) //handle normal form fields
                            {
                                formFields.add(item);
                            } else //handle uploaded file item.
                            {
                                files.add(item); //during album upload we should only have one file, however, to avoid writing a different pic upload function, we are storing in a Vector.
                            }
                        } //end iterator loop
                        if (files.size()>0) //there was a file in the upload request which was stored in the file array
                        {
                            User userBean = (User)request.getSession().getAttribute("userBean");
                            status = AlbumUtil.handleAlbumUpload(formFields, files, userBean.getId());
                            //System.out.println("Album status "+status);
                            if (status =="") //everything went well, return user to album page
                            {
                                request.getSession().setAttribute("page", "albums");
                                request.setAttribute("error", "<br>Your album was uploaded sucessfully");
                                dispatch.forward(request, response);
                            }else{
                                request.getSession().setAttribute("page", "addalbum");
                                request.setAttribute("error", status);
                                dispatch.forward(request, response);
                            }
                        } else{
                            status = "<br><strong>ERROR: No file was uploaded";
                            request.getSession().setAttribute("page", "addalbum");
                            request.setAttribute("error", status);
                            dispatch.forward(request, response);
                        }
                    } catch (Exception e) {e.printStackTrace();
                    System.out.println("Error parsing file upload");
                    status = "<br><strong>ERROR: Could not parse the upload request.  Image may be larger than maximum size limit of 3MB. Please try again</strong>";
                    request.getSession().setAttribute("page", "addalbum");
                    request.setAttribute("error", status);
                    dispatch.forward(request, response);
                    }
                    
                }//end if action = addalbum
                
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("addpic")&& request.getParameter("amount")!=null){ //user is requesting to upload multiple pictures, tell jsp to display correct form
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp");
                request.getSession().setAttribute("page", "addpic");
                request.setAttribute("amount", request.getParameter("amount"));
                dispatch.forward(request, response);
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("addpic")&& request.getParameter("amount")==null){
                String status = "";
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp");
                request.getSession().setAttribute("page", "addpic");
                if (isMultipart == true){ //handle uploading of picture files to album
                    System.out.println("multipart upload");
                    System.out.println(request.getParameter("Action"));
             /* Servlets have no native support for parsing a multipart post request that conform to RFC 1867, so we
              * are using a package from Jakarta to do this called Comons FileUpload.  Some of the following code is adapted from an example at jakarta.apache.org/commons/fileupload/using.html */
                    
                    status = ""; //need this to hold the result of the image upload and storage process. A empty status will mean everything is good
                    Vector formFields = new Vector(); //we'll store the form text items (not the picture) in this to send to the method that will process the actual storage of the image
                    Vector files = new Vector(); //store files from the request object
                    DiskFileUpload upload = new DiskFileUpload();
                    //set the max request size that we will accept.  This information comes from the config table
                    /* NOTE TO SELF, fix this later with config table */
                    upload.setSizeMax(4000000); //set to 4 megs for max upload size we will even look at (data and form fields).
                    String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                    
                    if (request.getSession().getAttribute("userBean")!=null) { //test to see if this is a logged in user and if this album belongs to him
                        User userBean = (User)request.getSession().getAttribute("userBean");
                        Album album = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                        if (album.getOwnerID().equals(userBean.getId())) {
                            userIsAlbumOwner = "true";
                        }
                    }
                    request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                    if (userIsAlbumOwner.equals("true")) //this should always be the case unless the user is trying to minipulate the system ot the session somehow expired
                    {
                        try{
                            List uploadItems = upload.parseRequest(request); //Parse the request into a list
                            //go through the list and handle the picture files and form items
                            Iterator iter = uploadItems.iterator();
                            while (iter.hasNext()){
                                FileItem item = (FileItem) iter.next(); //cast the List items that we obtained by parsing the request into file items
                                
                                if (item.isFormField()) //handle normal form fields
                                {
                                    
                                    formFields.add(item);
                                } else //handle uploaded file item.
                                {
                                    files.add(item);
                                    System.out.println("Processing a File upload request.."); //debug
                                }
                            } //end iterator loop
                            if (files.size()>0) //there was a file in the upload request which was stored in the file array
                            {
                                status = PictureUtil.handleImageUpload(formFields, files, (String)request.getSession().getAttribute("albumID"));
                            } else{
                                status = "<br><strong>ERROR: No file was uploaded";
                            }
                        } catch (Exception e) {e.printStackTrace();System.out.println("Error parsing file upload"); status = "<br><strong>ERROR: Could not parse the upload request.  Image may be larger than maximum size limit of 3MB. Please try again</strong>";}
                        if (status.substring(0, 2).equals("ID")) //image was stored without issue
                        {
                            status = "<br><Strong>Photo added to album successfully</strong>";
                        } else{ //we had some sort of problem storing the image, let the user know the error and return to the upload page.
                            status = "<br><Strong>Error storing photo</strong> "+status;
                        }
                    } //end if user owns album
                    else {status = "<br><Strong>Error, you do not own this album or other error occured</strong>";}
                } //end if multipart
                else //not a multipart message, should not happen, but check anyway
                {
                    status = "<br><strong>ERROR:</strong> No file was uploaded";
                }
                System.out.println("Image upload status:"+status);
                request.setAttribute("error", status);
                dispatch.forward(request, response);
            }//end if action = uploadpic
            else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("updatepic")){
                String status = "";
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp");
                request.getSession().setAttribute("page", "album");
                String picID = request.getParameter("id").trim();
                String title = request.getParameter("pictureTitle").trim();
                String description = request.getParameter("description").trim();
                String photographer = request.getParameter("photographer").trim();
                String date1 = request.getParameter("date1").trim();
                String date2 = request.getParameter("date2").trim();
                String date3 = request.getParameter("date3").trim();
                String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                if (request.getSession().getAttribute("userBean")!=null) { //test to see if this is a logged in user and if this album belongs to him
                    User userBean = (User)request.getSession().getAttribute("userBean");
                    Album album = AlbumUtil.getAlbumObject((String)request.getSession().getAttribute("albumID"));
                    if (album.getOwnerID().equals(userBean.getId())) {
                        userIsAlbumOwner = "true";
                    }
                }
                request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                if (userIsAlbumOwner.equals("true")) {
                    status = PictureUtil.handleImageUpdate(picID, title, description, photographer, date1, date2, date3);
                    if (status.equals("")==false) {
                        request.getSession().setAttribute("page", "updatepic");
                        request.setAttribute("error", status);
                        doGet(request, response);  //this will cause updatepic clause of doGet to run, hence giving the jsp the required info to populate the fields with again
                    }
                } else{
                    request.setAttribute("error", "<br><strong>ERROR:</strong> You are not the owner of this album!");
                }
                
                dispatch.forward(request, response);
                
            }else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("updatealbum")){ //handle updates to album data (not icon)
                String status = "";
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp");
                request.getSession().setAttribute("page", "albums");
                String albumID = request.getParameter("id").trim();
                String title = request.getParameter("title").trim();
                String description = request.getParameter("description").trim();
                String photographer = request.getParameter("photographer").trim();
                String albumVisibility = request.getParameter("albumVisibility");
                String userIsAlbumOwner = "false"; //used to tell the jsp if it should display items that only the owner should see
                if (request.getSession().getAttribute("userBean")!=null) { //test to see if this is a logged in user and if this album belongs to him
                    User userBean = (User)request.getSession().getAttribute("userBean");
                    Album album = AlbumUtil.getAlbumObject(albumID);
                    if (album.getOwnerID().equals(userBean.getId())) {
                        userIsAlbumOwner = "true";
                    }
                }
                request.setAttribute("userIsAlbumOwner", userIsAlbumOwner);
                if (userIsAlbumOwner.equals("true")) {
                    status = AlbumUtil.updateAlbumData(albumID, title, description, photographer, albumVisibility);
                    if (status.equals("")==false) {
                        request.getSession().setAttribute("page", "updatealbum");
                        request.setAttribute("error", status);
                        doGet(request, response); //this will cause updatealbum clause of doGet to run, hence giving the jsp the required info to populate the fields with again
                    }
                } else{
                    request.setAttribute("error", "<br><strong>ERROR:</strong> You are not the owner of this album!");
                }
                
                dispatch.forward(request, response);
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("addcomment")){ //handle adding a comment to a pic
                String status = "";
                String picID = request.getParameter("picid").trim();
                String userName = request.getParameter("userName").trim();
                String comment = request.getParameter("comment").trim();
                status = PictureUtil.addComment(picID, userName, comment);
                //System.out.println("Add Coment status is "+status);
                request.setAttribute("error", status);
                doGet(request, response);
                
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("search")){ //handle adding a comment to a pic
                ServletContext context = this.getServletContext();
                RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp");
                String status = "";
                String publicSearch = "";
                String keywords = "";
                String userName = "";
                String searchType = "";
                if (request.getParameter("public")!=null) {publicSearch = request.getParameter("public").trim();}
                if (request.getParameter("userName")!=null){userName = request.getParameter("userName").trim();}
                if (request.getParameter("keywords")!=null){keywords= request.getParameter("keywords").trim();}
                if (request.getParameter("searchType")!=null){searchType = request.getParameter("searchType").trim();} //the searchtype is used if the user already searched and more than 8results were returned
                if (request.getParameter("pos")!=null){request.setAttribute("pos",request.getParameter("pos") );} //this is used when more than 8 results are returned so that the use can view results 8 at a time
                else{request.setAttribute("pos","8");}
                if (publicSearch.equals("search")||searchType.equals("public")) //user requested all public albums
                {
                    Vector albumVector = AlbumUtil.getPubAlbums();
                    if (albumVector.size()==0) {
                        status = "<strong><br />No Results</strong>";
                    } else {
                        request.setAttribute("albumVector", albumVector);
                        Integer resultSize = new Integer (albumVector.size());
                        request.setAttribute("resultSize", resultSize );
                        request.setAttribute("searchType", "public");
                    }
                } //end all public albums
                else if (keywords.equals("")!=true ||searchType.equals("keywords")) //user requested all public albums
                {
                    if (request.getParameter("params")!=null){keywords = request.getParameter("params").trim();}
                    Vector albumVector = AlbumUtil.getPubAlbumsSearch(keywords);
                    if (albumVector.size()==0) {
                        status = "<strong><br />No Results</strong>";
                    } else {
                        request.setAttribute("albumVector", albumVector);
                        Integer resultSize = new Integer (albumVector.size());
                        request.setAttribute("resultSize", resultSize );
                        request.setAttribute("searchType", "keywords");
                        request.setAttribute("params", "keywords");
                    }
                } //end keywords
                else if (userName.equals("")!=true ||searchType.equals("userName")) //user requested all public albums
                {
                    if (request.getParameter("params")!=null){userName = request.getParameter("params").trim();}
                    String userID = UserUtil.getUserID(userName);
                    Vector albumVector = AlbumUtil.getPubAlbumsForUser(userID);
                    if (albumVector.size()==0) {
                        status = "<strong><br />No Results</strong>";
                    } else {
                        request.setAttribute("albumVector", albumVector);
                        Integer resultSize = new Integer (albumVector.size());
                        request.setAttribute("resultSize", resultSize );
                        request.setAttribute("searchType", "userName");
                        request.setAttribute("params", userName);
                    }
                } else{
                    status = "<strong><br />No Results</strong>";
                }
                request.setAttribute("error", status);
                
                dispatch.forward(request, response);
                
            } else if (request.getParameter("Action")!=null && request.getParameter("Action").equals("displayalbum")){
                doGet(request, response); //this handles the time when the user does an update.  This is due to the fact that the previous request was a post, so it's forwarded as a post, I suppose.
                
            }
        }
        
        
        catch (Exception e){ //some error occured, send user back to welcome page
            e.printStackTrace();
            ServletContext context = this.getServletContext();
            RequestDispatcher dispatch = context.getRequestDispatcher("/index.jsp"); //passes off to the jsp for display
            request.getSession().setAttribute("page", "welcome"); //store a session var that will be used by the jsp view to know what to display
            dispatch.forward(request, response);
        }
        
    } //end do post
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
// </editor-fold>
}
