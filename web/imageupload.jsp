<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

        <h1>Image Upload</h1>
    
        <form action="PicOps?Action=addpic" enctype="multipart/form-data"method="post">
            <table>
                <tr><td>Picture Title: </td><td><input name="pictureTitle" type="text" size="20" maxlength="50" /></td></tr>
                <tr><td>File: </td><td><input name="pictureFile" type="file" size="40" maxlength="260" /></td></tr><!--Note, 260 is the maximum path lenght for a windows 2000+ file path -->
                <%--               
                <tr><td>Email: </td><td><input name="email" type="text" size="20" maxlength="20" /></td></tr>
                <tr><td>Desired User Name: </td><td><input name="userName" type="text" size="20" maxlength="20" /></td></tr>
                <tr><td>Password: </td><td><input name="password" type="password" size="20" maxlength="20" /></td></tr>
                <tr><td>Confirm Password: </td><td><input name="password2" type="password" size="20" maxlength="20" /></td><td><font color ='red'>Passwords must be at least 6 characters</font></td></tr>
                --%>         

                <tr><td><input name="Submit" type="submit" value="submit" /></td><td><input name="Reset" type="reset" value="reset" /></td></tr>
            </table>
        </form>

  
    
    </body>
</html>
