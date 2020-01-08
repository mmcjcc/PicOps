<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<% //prevent caching of content.  Had some problems with my proxy server caching stuff and messing things up
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader("Expires", 0); //prevents caching at the proxy server
response.setContentType("text/html;ISO-8859-1");
%>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.1" prefix="str" %> <%-- Note, this taglin provides string formatting functions.  We needed it for the wordwrap function --%>

<%-- Interpret user's locale choice for number formatting.  Sometimes browser does not report locale, so to avoid errors, set it to english if the browser does not provide it--%>
   <c:choose>
       <c:when test="${param['locale'] != null}">
           <fmt:setLocale value="${param['locale']}" scope="session" />
       </c:when>
       <c:otherwise>
           <fmt:setLocale value="${param['en-US']}" scope="session" />
       </c:otherwise>
   </c:choose>

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>PicOps - Online Photo System</title>
        <meta http-equiv="Content-Type"
        content="text/html; charset=iso-8859-1" />
        <link rel="stylesheet" type="text/css"
        href="layout.css" />
        <link rel="stylesheet" type="text/css"
        href="presentation.css" />
       
        <script>
            function deleteconfirm()
            {
            return confirm("Are you sure you wish to delete this album?");
            }
	
            function deleteconfirmWrap()
            {
            if(deleteconfirm())
            window.location = "PicOps?Action=deletealbum"
            else
            alert("Album Not Deleted");
            }
            
            function deleteconfirmPic()
            {
            return confirm("Are you sure you wish to delete this Picture?");
            }
	
            function deleteconfirmWrapPic()
            {
            if(deleteconfirmPic())
            window.location = "PicOps?Action=deletepic"
            else
            alert("Picture Not Deleted");
            }
        </script>
    </head>
    <body>
        <div id="outer">
            <div id="hdr" align="center">
                <p class='top'> <strong>PicOps</strong> <br />
                <strong>O</strong>nline <strong>P</strong>hoto <strong>S</strong>ystem </p> 
            </div>
            <div id="bar"> <span style="padding:5px;font-size:11px;">
            
                <%//area for date %>
                <jsp:useBean id="now" class="java.util.Date" />
                <fmt:formatDate value ="${now}" type="both" timeStyle ="short" dateStyle="long"/>
                
            </span></div>

            <div id="bodyblock" align="right">
                <% //Right hand links section %>
                <div id="l-col" align="center">
                    <c:if test="${sessionScope.userBean.nameFirst !=null}"> <%-- Welcome message --%>
                        <strong>  Welcome ${sessionScope.userBean.nameFirst}!</strong><br /><br />
                    </c:if>   
                    <h4 align="center">Menu</h4>
                    <a href="PicOps">Home</a><br />
                    <a href="PicOps?Action=search">Search</a><br />
                    <c:if test="${sessionScope.loggedIn !=true || sessionScope.userBean ==null}"> <%-- links for user not logged in --%>
                        <a href="PicOps?Action=newuser">New User Signup</a><br />
                        <a href="PicOps?Action=forgotpassword">Forgot Password</a><br />
                        <a href = "mailto:picops@ezjcc.com">Contact</a><br />
                        <a href="PicOps?Action=login">Login</a><br />
                    </c:if> 
               
                    <c:if test="${sessionScope.loggedIn ==true && sessionScope.userBean !=null}"> <%-- links for user logged in --%>
                        <a href="PicOps?Action=passwordchange">Change Password</a><br />
                        <a href = "mailto:picops@ezjcc.com">Contact</a><br />
                        <a href="PicOps?Action=logout">Log Out</a><br /> 
                        <br />
                        <h4 align = "center" class = "redtext">Album Actions</h4>
                        <a href="PicOps?Action=displayalbums">View Albums</a><br />
                        <a href="PicOps?Action=addalbum">Add Album</a><br /> 
                    </c:if> 
                
                    <c:if test="${(sessionScope.page =='displayslideshow' && requestScope.userIsAlbumOwner == 'true') || (sessionScope.page =='displayalbum' && requestScope.userIsAlbumOwner == 'true')}">
                     
   <a href="PicOps?Action=displayalbum&id=${sessionScope.albumID}">View as Thumbnails</a><br />
   <a href="PicOps?Action=displayslideshow&id=0">View as Slideshow</a><br />
   <a href=PicOps?Action=addpic>Add Photos to Album </a><br />
   <a href="javascript:deleteconfirmWrap()">Delete Album</a> <br />
   <c:if test="${sessionScope.page =='displayslideshow'}">
       <br />
       <h4 align = "center" class = "redtext">Photo Actions</h4>
       <a href="javascript:deleteconfirmWrapPic()">Delete This Picture</a> <br />
       <a href=PicOps?Action=updatepic&id=${image.id}>Update This Picture </a><br />
   </c:if>
                                                                                        
                    </c:if>
                    
                    <c:if test="${(sessionScope.page =='displayslideshow' && requestScope.userIsAlbumOwner != 'true') || (sessionScope.page =='displayalbum' && requestScope.userIsAlbumOwner != 'true')}">
   <br />
   <h4 align = "center" class = "redtext">Album Actions</h4>
   <a href="PicOps?Action=displayalbum&id=${sessionScope.albumID}">View as Thumbnails</a><br />
   <a href="PicOps?Action=displayslideshow&id=0">View as Slideshow</a><br />
                                                                                                               
                    </c:if>
                    
                    <c:if test="${sessionScope.page =='addpic' || sessionScope.page =='updatepic' || sessionScope.page=='updatealbum'}">
                        <br />
                        <h4 align = "center" class = "redtext">Album Actions</h4>
                        <a href="PicOps?Action=displayalbum&id=${sessionScope.albumID}">Return to Album</a><br />
                    </c:if>
                    
                    <p></p>
                </div>
            
                <div id="cont">
                    <!--This is the main body section--> 
                    <c:if test="${sessionScope.page == null}"> <%-- page not set, forward to servlet to set page --%>
                        <jsp:forward page="/PicOps"/>   
                    </c:if>
                    <c:if test="${sessionScope.page =='welcome'}"> <%-- page set to welcome, include welcome jsp file --%>
                        <%@include file="welcome.jsp"%> 
                    </c:if> 
                 
                    <c:if test="${sessionScope.page =='newusersignup'}"> <%-- page set to newusersignup, include ewusersiggnup jsp file --%>
                        <%@include file="newusersignup.jsp"%> 
                    </c:if> 
                 
                    <c:if test="${sessionScope.page =='emailsent'}"> <%-- page set to emailsent, include emailsent jsp file --%>
                        <%@include file="emailsent.jsp"%> 
                    </c:if> 
                
                    <c:if test="${sessionScope.page =='login'}"> <%-- page set to login, include welcome login file --%>
                        <%@include file="login.jsp"%> 
                    </c:if> 
                    <c:if test="${sessionScope.page =='albums'}"> <%-- page set to albums, forward to servlet to process request --%>
                        <jsp:forward page="/PicOps?Action=displayalbums"/> 
                    </c:if> 
                    <c:if test="${sessionScope.page =='displayalbums'}"> <%-- page set to albums, forward to servlet to process request --%>
                        <%@include file="albums.jsp"%> 
                    </c:if> 
                    <c:if test="${sessionScope.page =='displayalbum'}"> <%-- page set to album, include album jsp --%>
                        <%@include file="album.jsp"%> 
                    </c:if> 
                    
                    <c:if test="${sessionScope.page =='displaypubalbums'}"> <%-- page set to album, include album jsp --%>
                        <%@include file="pubalbums.jsp"%> 
                    </c:if>
                    <c:if test="${sessionScope.page =='album'}"> <%-- page set to album, forward to servlet to get the album contents --%>
                        <jsp:forward page="/PicOps?Action=displayalbum&id=${sessionScope.albumID}"/>
                    </c:if>
                    <c:if test="${sessionScope.page =='addalbum'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="addalbum.jsp"%> 
                    </c:if>
                    <c:if test="${sessionScope.page =='addpic'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="addpic.jsp"%> 
                    </c:if>
                    <c:if test="${sessionScope.page =='updatepic'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="updatepic.jsp"%> 
                    </c:if>
                    <c:if test="${sessionScope.page =='updatealbum'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="updatealbum.jsp"%> 
                    </c:if>
                    <c:if test="${sessionScope.page =='changepassword'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="changepassword.jsp"%> 
                    </c:if> 
                    <c:if test="${sessionScope.page =='displayslideshow'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="slideshow.jsp"%> 
                    </c:if> 
                    <c:if test="${sessionScope.page =='slideshow'}"> <%-- page set to albums, include albums jsp file --%>
                        <jsp:forward page="/PicOps?Action=displayslideshow"/> 
                    </c:if> 
                    <c:if test="${sessionScope.page =='forgotpassword'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="forgotpassword.jsp"%> 
                    </c:if>
                    <c:if test="${sessionScope.page =='search'}"> <%-- page set to albums, include albums jsp file --%>
                        <%@include file="search.jsp"%> 
                    </c:if>
                </div> 
                <div id="ftr" align="center"><p class="footer"> Copyright (c) Jason Cohen 2005 </p></div>
            </div>
        </div>
    </body>
</html>
