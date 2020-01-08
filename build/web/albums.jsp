<h1>Your Photo Albums</h1><hr>
<c:if test="${sessionScope.loggedIn !='true' || sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
          <c:set var="page" value="welcome" scope ="session"/>           
    <jsp:forward page="/PicOps"/> 
</c:if> 
<c:if test="${RequestContext.error != null}"> <%-- we have an error, display the message --%>
    <p class = "redtextmedium"><strong>${RequestContext.error}</strong>
</c:if> 
<c:if test="${RequestContext.error == null}">
    <p class = "redtextmedium">Welcome ${userBean.nameFirst}! </p>
</c:if>

<p class="navytext"> You currently have ${numAlbums} albums on PicOps, shown below. Your albums are using<span style="color:red"> <fmt:formatNumber value = "${storageUsage/1048576}" pattern="####.##"/> </span>MB of space out of<span style="color:red"> ${quota}</span> MB.
You have <span style="color:red"><fmt:formatNumber value = "${((quota -(storageUsage/1048576))/quota)*100 }" pattern="###.##"/>%</span> Space Free </p><p class="navytext"> If you would like to give out a link for your public albums, it is <br/ ><a  class = "redlink" href= "${webaddress}/PicOps/PicOps?Action=displaypubalbums&user=${userBean.userName}">http://picops.ezjcc.com/PicOps/PicOps?Action=displaypubalbums&user=${userBean.userName}</a></p>

<c:if test="${albumVector != null}">
    <table class="albums" cellpadding = "10" cellspacing="10" border="1" align="center">
        <tr>
        <c:set var="i" value="1" scope ="page"/>
        <c:forEach items="${albumVector}" var = "album"> <%-- we have some albums, display icons for them as links to full album--%>

        <td> <a  href="PicOps?Action=displayalbum&id=${album.id}" class= "picborder"> <img src =PicOps?Action=displaythumbnail&id=${album.icon} alt="album icon for ${album.title}"></a>
            <br><a class="redlink" href=PicOps?Action=updatealbum&id=${album.id}>Update This Info </a>
            <br><str:wordWrap width ="37" delimiter = "<br />">Title: ${album.title} </str:wordWrap> 
            <br>Visibility: ${album.albumVisibility} 
            <c:if test="${album.photographer != null}">
                <br><str:wordWrap width ="37" delimiter = "<br />">Photographer: ${album.photographer} </str:wordWrap> 
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