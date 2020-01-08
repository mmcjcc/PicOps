<h1>Photo Album </h1><hr>
<c:if test="${requestScope.userIsAlbumOwner ==null && sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
    <c:set var="page" value="welcome" scope ="session"/>           
    <jsp:forward page="/PicOps"/> 
</c:if> 
<c:if test="${requestScope.error != null}"> <%-- we have an error, display the message --%>
    <p class = "redtextmedium"><strong>${requestScope.error}</strong> </p>
</c:if> 
<c:if test="${requestScope.error == null}">
    <p class = "redtextmedium">Photo album Contents </p>
    <c:if test="${requestScope.userIsAlbumOwner != 'true'}"> 
        <p>The owner of this album is <strong><a  class = "redlink" href= "${webaddress}/PicOps/PicOps?Action=displaypubalbums&user=${albumOwnerName}">${albumOwnerName}</a></strong></p>
    </c:if>
</c:if>

<c:if test="${albumContents != null}">
    <table class = "showphotoinfo">
        <tr><td>Show photo info?</td> 
        <td><form name = showForm action ="PicOps?Action=displayalbum&id=${album.id}" method = "post">
            <select class = "input-box2" name = showinfo size=1 onChange="showForm.submit();">
                <option value = "yes"></option>
                <option value = "yes">yes</option>
                <option value = "no">no</option>
            </select>
        </form></td></tr>
    </table>
    
    <table class="pictures" cellpadding = "10" cellspacing="10" border="1" >
        <tr>
        <c:set var="i" value="1" scope ="page"/>
        <c:forEach items="${albumContents}" var = "image"> <%-- we have some albums, display icons for them as links to full album--%>
          
        <td> 
            <a  href= PicOps?Action=displayslideshow&id=${i -1} class= "picborder"> <img src =PicOps?Action=displaythumbnail&id=${image.id} alt="album icon for ${image.imageTitle}"></a><br />
            <c:if test="${image.id == albumIcon}">
                <font class="bold10">Album Icon</font><br />
            </c:if>
            <c:if test="${requestScope.userIsAlbumOwner == 'true'}">
                <c:if test="${image.id != albumIcon}">
                    <a class = "redlink" href="PicOps?Action=changeicon&id=${image.id}">Make Album Icon</a><br />
                </c:if>
                <a class = "redlink" href=PicOps?Action=updatepic&id=${image.id}>Update This Picture </a><br />
                <a class = "redlink" href=PicOps?Action=deletepic&id=${image.id}>Delete This Picture </a><br />
            </c:if>
            <c:if test="${showinfo == 'yes'}">
                <br />Title:  <str:wordWrap width ="37" delimiter = "<br /">${image.imageTitle}</str:wordWrap>
                <br />Photographer:  <str:wordWrap width ="37" delimiter = "<br />">${image.imagePhotographer} </str:wordWrap>
                <br /> Description: <c:if test = "${fn:length(image.imageDescription)>15}">
                 ${fn:substring(image.imageDescription,0,10)}...
                </c:if>
                <c:if test = "${fn:length(image.imageDescription)<15}">
                 ${image.imageDescription}
                </c:if>
                <br />Taken On:  ${image.imageDate}
                <br />File Name: <str:wordWrap width ="27" delimiter = "<br />"> ${image.fileName}</str:wordWrap>
                <br />Size:  <fmt:parseNumber value = "${image.pictureSize /12}" integerOnly="true"/> KB
                <br />Upload Date:   <fmt:formatDate value ="${image.uploadDate}" type="both" timeStyle ="short" dateStyle="short"/>
            </c:if>
        </td>
        <c:if test="${i % 4 == 0 }">
            </tr><tr>
        </c:if> 
            <c:set var="i" value="${i +1}" scope ="page"/>  
            </c:forEach>
        </tr>
    </table>
    <p></p>
    <table class = "albuminfo">
        <tr><td colspan=2 align = "center"><strong>Album Info</strong></td></tr>
        <c:if test="${requestScope.userIsAlbumOwner == 'true'}"> 
            <tr><td colspan=2 align = "center"> <a class="redlink" href=PicOps?Action=updatealbum&id=${album.id}><strong>Update This Info </strong></a></td></tr>
        </c:if>
        <tr><td><str:wordWrap width ="37" delimiter = "<br />"><strong>Album Title:</strong> </td><td>${album.title}</str:wordWrap>&nbsp</td></tr>
        <tr><td><str:wordWrap width ="37" delimiter = "<br />"><strong>Album Photographer:</strong></td><td> ${album.photographer}</str:wordWrap>&nbsp</td></tr>
        <tr><td><strong>Visibility: </strong></td><td>${album.albumVisibility}&nbsp</strong></td></tr>
        <tr><td><strong>Description:</strong></td><td><str:wordWrap width ="37" delimiter = "<br />">${album.description}</str:wordWrap>&nbsp</td></tr>
        <tr><td><strong>Creation Date:</strong></td><td> <fmt:formatDate value ="${album.creationDate}" type="both" timeStyle ="short" dateStyle="short"/>
        </td></tr>
    </table>
    
</c:if>

<p></p>