<h1> Update Photo Album </h1>
<c:if test="${sessionScope.loggedIn !='true' || sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
          <c:set var="page" value="welcome" scope ="session"/>        
    <jsp:forward page="/PicOps"/> 
</c:if> 
<hr />

<c:if test="${requestScope.error ==null}"> <%-- display the message as user has not submitted the form yet--%>
    <p class="navytext">Please fill in the form below to upload a photo album. The photo that you select to be the icon for the album will also be added to the photo album and does not need to be a certain size but it must be less than 3MB in file size.</p>
    
    <p class = "redtextbold"> Please fill in all required fields. </p>
</c:if> 

<c:if test="${requestScope.error !=null}"> <%-- we have a signup error, display the message --%>
    <p class = "redtextbig"><strong>Error while attempting to upload album</strong>
    <p class=redtext>${requestScope.error} </p>
</c:if> 
<form action="PicOps?Action=updatealbum&id=${album.id}" method="post">
    <table class = "addalbum"">
        <tr><td>Album Title: </td><td><input name="title" class="input-box2" type="text" size="52" maxlength="200" value = "${album.title}"/></td><td class="redtext">Required</td></tr>
        <tr><td>Album Photographer: </td><td><input class="input-box2" name="photographer" type="text" size="52" maxlength="100" value = "${album.photographer}"/></td>
         <tr><td>Public or Private Album?: </td><td><select class="input-box2" name="albumVisibility">
            <option value="public">public</option>
            <option value="private">private</option>
          </select></td><td class="redtext">Required</td></tr>
         <tr><td>Description:</td><td><textarea class="input-box2" name="description" cols = 40 rows = 6 >${album.description}</textarea></td></tr>
         <tr><td><input class="submit-button-small" name="Submit" type="submit" value="submit" /></td><td><input class="submit-button-small" name="Reset" type="reset" value="reset" /></td></tr>
    </table>
</form>
