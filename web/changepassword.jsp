<c:if test="${sessionScope.loggedIn !='true' || sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
           <c:set var="page" value="welcome" scope ="session"/>          
    <jsp:forward page="/PicOps"/> 
</c:if> 
<h1>Change Your Password</h1>
<hr />
<c:if test="${error ==null}"> <%-- display the message as user has not submitted the form yet--%>
    <p>You may change your password using the form below.  Please enter your old password and your new password twice.  Passwords must be a minimum of 6 characters in length</p>
</c:if> 

<c:if test="${error !=null}"> <%-- we have a signup error, display the message --%>
     <p class=redtext>${error} </p>
</c:if> 
<form action="PicOps?Action=changepassword" method="post">
    <table align = "center">
        <tr><td>Old Password: </td><td><input name="oldpassword" type="password" size="20" maxlength="20" /></td></tr>
        <tr><td>Password: </td><td><input name="newpassword" type="password" size="20" maxlength="20" /></td></tr>
        <tr><td>Confirm Password: </td><td><input name="newpassword2" type="password" size="20" maxlength="20" /></td><td><font color ='red'>Passwords must be at least 6 characters</font></td></tr>
        <tr><td><input name="Submit" type="submit" value="submit" /></td><td><input name="Reset" type="reset" value="reset" /></td></tr>
    </table>
</form>

