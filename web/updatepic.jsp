<h1>Update Picture Info</h1><hr>

<c:if test="${sessionScope.loggedIn !='true' || sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
          <c:set var="page" value="welcome" scope ="session"/>      
    <jsp:forward page="/PicOps"/> 
</c:if> 

<c:if test="${requestScope.error != null}"> <%-- we have an error, display the message --%>
    <p class = "redtextmedium"><strong>${requestScope.error}</strong></p>
</c:if> 
<p align = "left">&nbsp<img  src =PicOps?Action=displaythumbnail&id=${image.id} alt="album icon for ${image.imageTitle}"></a></p>
<p class = "redtext" > Please enter new values for the following optional information about this picture </p>
        <form action="PicOps?Action=updatepic&id=${image.id}" method="post">
            <table class ="addpic" align="center">
                <tr><td>Picture Title: </td><td><input class="input-box2" name="pictureTitle" type="text" size="20" maxlength="50" value="${requestScope.image.imageTitle}"/></td></tr>
                <tr><td>Photographer: </td><td><input class="input-box2" name="photographer" type="text" size="52" maxlength="100" value = "${image.imagePhotographer}" /></td>
                <c:set var="dateArray" value="${fn:split(image.imageDate, '/')}" scope ="page"/>
                <tr><td>Photo Taken On:</td><td>MM<input class="input-box2" name="date1" type="text" size="2" maxlength="2" value = "${dateArray[0]}"/>DD<input class="input-box2" name="date2" type="text" size="2" maxlength="2" value = "${dateArray[1]}"/>YYYY<input class="input-box2" name="date3" type="text" size="4" maxlength="4" value = "${dateArray[2]}"/></td>
                <tr><td>Description:</td><td><textarea class="input-box2" name="description" cols = 40 rows = 6>${image.imageDescription}</textarea></td></tr>
            <tr><td><input name="Submit" type="submit" value="submit" class="submit-button-small"/></td><td><input name="Reset" type="reset" value="reset" class="submit-button-small"/></td></tr>
            </table>
        </form>
