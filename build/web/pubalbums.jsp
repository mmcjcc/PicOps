<h1>Public Albums for ${userName}</h1><hr>

<c:if test="${RequestContext.error != null}"> <%-- we have an error, display the message --%>
    <p class = "redtextmedium"><strong>${RequestContext.error}</strong>
</c:if> 
<c:if test="${RequestContext.error == null}">
    <p class = "redtextmedium">Welcome ${userBean.nameFirst}! </p>
</c:if>

<p>${userName}  currently has ${numAlbums} public albums on PicOps, shown below.</p>

<c:if test="${albumVector != null}">
    <table class="albums" cellpadding = "10" cellspacing="10" border="1" width="700" align="center">
        <tr>
        <c:set var="i" value="1" scope ="page"/>
        <c:forEach items="${albumVector}" var = "album"> <%-- we have some albums, display icons for them as links to full album--%>
 
        <td> <a  href="PicOps?Action=displayalbum&id=${album.id}" class= "picborder"> <img src =PicOps?Action=displaythumbnail&id=${album.icon} alt="album icon for ${album.title}"></a>
           
            <br>Title: ${album.title}  
            <br>Visibility: ${album.albumVisibility} 
            <c:if test="${album.photographer != null}">
                <br>Photographer: ${album.photographer}
            </c:if>
            <c:if test="${album.description != null}">
                <br>Description: 
                <c:if test = "${fn:length(album.description)>15}">
                 ${fn:substring(album.description,0,25)}...
                </c:if>
                <c:if test = "${fn:length(album.description)<15}">
                 ${album.description}
                </c:if>
            </c:if>
            <br>Creation Date: ${album.creationDate}
        </td>
        <c:if test="${i % 4 == 0}">
            </tr><tr>
        </c:if> 
            <c:set var="i" value="${i +1}" scope ="page"/>  
            </c:forEach>
        </tr>
    </table>
</c:if>
<p></p>