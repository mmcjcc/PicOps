<h1>Add a Picture</h1><hr>

<c:if test="${sessionScope.loggedIn !='true' || sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
          <c:set var="page" value="welcome" scope ="session"/>           
    <jsp:forward page="/PicOps"/> 
</c:if> 

<c:if test="${requestScope.error != null}"> <%-- we have an error, display the message --%>
    <p class = "redtextmedium"><strong>${requestScope.error}</strong></p>
</c:if> 
<c:if test="${multiple == null }">
    <p class = "navytext" >Would you like to add more than one Photo at this time?  Note, you will not be able to add data about the photo until after the upload and you cannot upload more than 4MB of photos at a time.</p>
    <a href="PicOps?Action=addpic&multiple=true" class="button">Add Multiple?</a></td>
</c:if>

<c:if test="${multiple == true}">
<p class = "navytext"> How many photos would you like to add? 
    <form name = amountForm action ="PicOps?Action=addpic" method = "post">
        <select name = amount size=1 onChange="amountForm.submit();">
            <option value = ""></option>
            <option value = "1"> 1</option>
            <option value = "2"> 2</option>
            <option value = "3"> 3</option>
            <option value = "4"> 4</option>
            <option value = "5"> 5</option>
        </select>
    </form>
    </p>
</c:if>
<c:if test="${amount == null || amount == 1}">
<p class = "redtext" > Please select a photo to add to your album. Photos cannot be larger than 3MB </p>
        <form action="PicOps?Action=addpic" enctype="multipart/form-data"method="post">
            <table class ="addpic" align="center">
                <tr><td>Picture Title: </td><td><input class="input-box2" name="pictureTitle" type="text" size="20" maxlength="50" /></td></tr>
                <tr><td>Photographer: </td><td><input class="input-box2" name="photographer" type="text" size="52" maxlength="100" /></td>
                <tr><td>Photo Taken On:</td><td>MM<input class="input-box2" name="date1" type="text" size="2" maxlength="2" />DD<input class="input-box2" name="date2" type="text" size="2" maxlength="2" />YYYY<input class="input-box2" name="date3" type="text" size="4" maxlength="4" /></td>
                <tr><td>Description:</td><td><textarea class="input-box2" name="description" cols = 40 rows = 6></textarea></td></tr>
                <tr><td>File: </td><td><input class="input-box2" name="pictureFile" type="file" size="39" maxlength="260" /></td><td class="redtext">Required</td></tr><!--Note, 260 is the maximum path lenght for a windows 2000+ file path -->
                <tr><td><input name="Submit" type="submit" value="submit" class="submit-button-small"/></td><td><input name="Reset" type="reset" value="reset" class="submit-button-small"/></td></tr>
            </table>
        </form>
</c:if>

<c:if test="${amount != null && amount > 1}">
<p class = "redtext" > Please select photos to add to your album. Photos cannot be larger than 3MB </p>
        <form action="PicOps?Action=addpic" enctype="multipart/form-data"method="post">
            <table class ="addpic" align="center">
           <c:forEach begin="${1}" end = "${amount}" step="${1}">
           <tr><td>File: </td><td><input class="input-box2" name="pictureFile" type="file" size="39" maxlength="260" /><td>
           </c:forEach>
                <tr><td><input name="Submit" type="submit" value="submit"  class="submit-button-small"/></td><td><input name="Reset" type="reset" value="reset"  class="submit-button-small"/></td></tr>
            </table>
        </form>
</c:if>