<h1>Photo Album Slideshow</h1><hr>
 <script type="text/javascript" src="minmax.js"></script>  <%--This is a totaly stupid solution for the fact that IE does not support the max-height css property, and I happen to need it Thanks to http://www.doxdesk.com/software/js/minmax.html --%>
<c:if test="${requestScope.error != null}"> <%-- we have an error, display the message --%>
    <p class = "redtextmedium"><strong>${requestScope.error}</strong>
</c:if> 
<c:if test="${requestScope.error == '' || requestScope.error == null}">
    <c:set var="picID" value="${requestScope.image.id}" scope ="session"/> <%-- var used during delete --%>
    <div id="slideshow">
     <p align = "center">
        
       <table class = "centered" align = "center">
            <tr>
                <td><a href="PicOps?Action=displayslideshow&id=${requestScope.imagePos -1}#filmstrip" class="button" id="buttonOK">Previous</a></td>
                <td><a href="PicOps?Action=displayslideshow&id=${requestScope.imagePos +1}#filmstrip" class="button" id="buttonOK">Next</a></td>
            </tr>
            <a name = "filmstrip"/>
            <tr><td colspan=2 >
                <a href="PicOps?Action=displaypic&id=${requestScope.image.id}" class= "picborder" target = "_blank"> <img src =PicOps?Action=displaypic&id=${requestScope.image.id}&size=600 alt=" ${requestScope.image.fileName}"> </a>
            </td>
            </tr>
        </table>
    </div>
        <table class="filmstrip"> <tr>
        
        
        <c:choose>  <%--display filmstrip --%>
            <c:when test='${albumContents[imagePos-2] !=null}'>
               <td> <a  href= "PicOps?Action=displayslideshow&id=${imagePos -2}#filmstrip" class= "picborder"> <img class = "film" src ="PicOps?Action=displaythumbnail&id=${albumContents[imagePos-2].id} "></a> </td>
            </c:when>
            <c:otherwise>
                
            </c:otherwise>
        </c:choose>
      
        
        <c:choose>
            <c:when test='${albumContents[imagePos-1] !=null}'>
             <td>   <a  href= "PicOps?Action=displayslideshow&id=${imagePos -1}#filmstrip" class= "picborder"> <img class = "film"src ="PicOps?Action=displaythumbnail&id=${albumContents[imagePos-1].id} "></a></td>
            </c:when>
            <c:otherwise>
              <td> end </td>
            </c:otherwise>
        </c:choose>
        
        <td><img class = "film" src= "PicOps?Action=displaythumbnail&id=${image.id}#filmstrip"> </td>
        
         <c:choose>
            <c:when test='${albumContents[imagePos+1] !=null}'>
                <td><a  href= "PicOps?Action=displayslideshow&id=${imagePos +1}#filmstrip" class= "picborder"> <img class = "film"src ="PicOps?Action=displaythumbnail&id=${albumContents[imagePos+1].id} "></a></td>
            </c:when>
            <c:otherwise>
               <td> end </td> 
            </c:otherwise>
        </c:choose>
        
        
         <c:choose>
            <c:when test='${albumContents[imagePos+2] !=null}'>
                <td><a  href= "PicOps?Action=displayslideshow&id=${imagePos +2}#filmstrip" class= "picborder"> <img class = "film"src ="PicOps?Action=displaythumbnail&id=${albumContents[imagePos+2].id} "></a>
             </td>
            </c:when>
            <c:otherwise>
               
            </c:otherwise>
        </c:choose>
       
        </tr> </table>
       
    </p>
    <table class = "pictureinfoaddcomment">
    <tr valign="top"><td>
        <table class="picturesbig" align = center>
            <tr><td colspan=2 align = "center"><strong>Picture Info</strong></td></tr>
            <c:if test="${requestScope.userIsAlbumOwner == 'true'}" >
                <tr><td colspan="2" align = "center"><strong><a class="redlink" href=PicOps?Action=updatepic&id=${image.id}>Update This Picture </a></strong></td></tr>
            </c:if>   
            <tr><td >Title:</td><td>  <str:wordWrap width ="30" delimiter = "<br />">${image.imageTitle}</str:wordWrap>&nbsp</td</tr>
            <tr><td>Photographer:</td><td> <str:wordWrap width ="30" delimiter = "<br />"> ${image.imagePhotographer}</str:wordWrap>&nbsp</td></tr>
            <tr><td> Description:</td><td> <str:wordWrap width ="30" delimiter = "<br />">${image.imageDescription}</str:wordWrap>&nbsp</td></tr>
            <tr><td>Taken On:</td><td>  ${image.imageDate}&nbsp</td></tr>
            <tr><td>File Name: </td><td> <str:wordWrap width ="30" delimiter = "<br />"> ${image.fileName}</str:wordWrap>&nbsp</td></tr>
            <tr><td>Size:</td> <td> <fmt:parseNumber value = "${image.pictureSize /12}" integerOnly="true"/> KB &nbsp</td></tr>
            <tr><td>Upload Date: </td><td> <fmt:formatDate value ="${image.uploadDate}" type="both" timeStyle ="short" dateStyle="short"/>&nbsp</td></tr>
        </table>
    </td><td valign="top">
    
        <table align = "center" class = "addcomments">
            <tr><td colspan=2 align = "center"><strong>Add a Comment</strong></td></tr>
            <form action="PicOps?Action=addcomment&id=${requestScope.imagePos}&picid=${image.id}" method="post">
                <tr><td>Your Name: </td><td><input class="input-box" name="userName" type="text" size="30" maxlength="50" value = "${userBean.nameFirst}" /></td>
                <tr><td>Comment:</td><td><textarea class="input-box" name="comment" cols = 30 rows = 6></textarea></td></tr>
                <tr><td><input name="Submit" type="submit" value="submit" class="submit-button-small"/></td><td><input name="Reset" type="reset" value="reset" class="submit-button-small"/></td></tr>
            </form>
        </table>
    </td></tr></table>
    <c:if test = "${comments != null}">
        <p></p>
        <table align = "center" class = "comments" >
            <tr><td colspan="2" align = "center"><strong>Comments</strong><hr /></td></tr>
            <tr><td>
                <c:set var="i" value="0" scope ="page"/>
        
                <c:forEach items="${comments}" var = "comm"> 
                <table <c:if test="${i % 2 == 0}">class="commentcolor" </c:if><c:if test="${i % 2 != 0}">class="comment" </c:if>>
                    <c:if test="${requestScope.userIsAlbumOwner == 'true'}" > <tr><td colspan="2" align = "center"> <a class="redlink" href=PicOps?Action=deletecomment&id=${requestScope.imagePos}&cid=${comm.id}><strong>Delete Comment</strong></a></td></tr> </c:if>
                    <tr><td>Name:</td><td><str:wordWrap width ="60" delimiter = "<br />"><c:out value = "${comm.userName}"/></str:wordWrap>&nbsp</td></tr>
                    <tr  valign = "top"><td>Comment:</td><td><str:wordWrap width ="60" delimiter = "<br />"><c:out value = "${comm.comment}"/></str:wordWrap>&nbsp</td></tr>
                    <tr ><td>Date:</td><td> <fmt:formatDate value ="${comm.timestamp}" type="both" timeStyle ="short" dateStyle="short"/></td></tr>
                
                </table>
            </td><tr><td colspan="2"><hr /></td></tr><tr><td>
                <c:set var="i" value="${i +1}" scope ="page"/>  
                </c:forEach>
            </td></tr>
        </table>
    </c:if>
</c:if>
        <br />