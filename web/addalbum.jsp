<h1> Add a Photo Album </h1>
<c:if test="${sessionScope.loggedIn !='true' || sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
         <c:set var="page" value="welcome" scope ="session"/>          
    <jsp:forward page="/PicOps"/> 
</c:if> 
<hr />

<c:if test="${requestScope.error ==null}"> <%-- display the message as user has not submitted the form yet--%>
    <p class= "navytext">Please fill in the form below to upload a photo album. The photo that you select to be the icon for the album will also be added to the photo album and does not need to be a certain size but it must be less than 3MB in file size.</p>
    
    <p class = "redtextbold"> Please fill in all required fields. </p>
</c:if> 

<c:if test="${requestScope.error !=null}"> <%-- we have a signup error, display the message --%>
    <p class = "redtextbig"><strong>Error while attempting to upload album</strong>
    <p class=redtext>${requestScope.error} </p>
</c:if> 
<form action="PicOps?Action=addalbum" enctype="multipart/form-data" method="post">
    <table class = "addalbum">
        <tr><td>Album Title: </td><td><input class="input-box2" name="title" type="text" size="52" maxlength="200"  /></td><td class="redtext"></td></tr>
        <tr><td>Album Photographer: </td><td><input class="input-box2" name="photographer" type="text" size="52" maxlength="100" /></td>
         <tr><td>Public or Private Album?: </td><td><select class="input-box2" name="albumVisibility">
            <option value="public">public</option>
            <option value="private">private</option>
          </select></td><td class="redtext">Required</td></tr>
         <tr><td>Description:</td><td><textarea class="input-box2" name="description" cols = 40 rows = 6></textarea></td></tr>
        <tr><td>File: </td><td><input name="pictureFile" class="input-box2" type="file" size="39" maxlength="260" /></td><td class="redtext">Required</td></tr><!--Note, 260 is the maximum path lenght for a windows 2000+ file path -->
        <tr><td><input name="Submit" type="submit" value="submit"class="submit-button-small" /></td><td><input name="Reset" type="reset" value="reset" class="submit-button-small"/></td></tr>
    </table>
</form>

