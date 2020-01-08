<h1>Search</h1><hr>

<p class="navytext">You can search for public albums by keywords (such as photographer, title, and description) or by user name or display all public albums
</p> 
<fieldset class="fieldsearchform"> <br />
   <legend>Search</legend>
      <form action="PicOps?Action=search" method="post">  <label class="searchform">Keywords:</label><input name="keywords" type="text" size="40" maxlength="200" class="input-box"/>&nbsp&nbsp<input name="Submit" type="submit" value="search" class="submit-button-search"/> </form> <br />
      <form action="PicOps?Action=search" method="post">  <label class="searchform">User Name:</label><input name="userName" type="text" size="40" maxlength="50"class="input-box"/>&nbsp&nbsp<input name="Submit" type="submit" value="search" class="submit-button-search"/> </form> <br />
      <form action="PicOps?Action=search" method="post"> <label class="searchform"> Show All Public?</label><input name="public" type="submit" value="search" class="submit-button-search"/></form> <br />
     
</fieldset>
<c:if test="${requestScope.error != null}"> <%-- we have an error, display the message --%>
    <p class = "redtextmedium"><strong>${requestScope.error}</strong> </p>
</c:if> 

<c:if test="${albumVector != null}">
<h3>Search Results <br><strong>${resultSize} Matches<br />Displaying ${pos-8} - <c:if test="${pos+1 <= resultSize}"> ${pos} </c:if> <c:if test="${pos+1 >resultSize}"> ${resultSize} </c:if></strong></h3> 
    <table class="albums" cellpadding = "10" cellspacing="10" border="1" width="700" align="center">
        <tr>
        <c:set var="i" value="1" scope ="page"/>
        <c:forEach items="${albumVector}" var = "album"> <%-- we have some albums, display icons for them as links to full album--%>
        <c:if test="${i <= pos && i > pos-8}">
        <td> <a  href="PicOps?Action=displayalbum&id=${album.id}" class= "picborder"> <img src =PicOps?Action=displaythumbnail&id=${album.icon} alt="album icon for ${album.title}"></a>
           
            <br>Title: <str:wordWrap width ="30" delimiter = "<br />"> ${album.title}  </str:wordWrap> 
            <c:if test="${album.photographer != null}">
                <br>Photographer: <str:wordWrap width ="30" delimiter = "<br />">${album.photographer}</str:wordWrap> 
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
        </c:if>
            <c:set var="i" value="${i +1}" scope ="page"/>  
            </c:forEach>
        </tr>
    </table>
    <c:if test="${pos+1 > resultSize && resultSize!= pos && resultSize > 8}"> <a href="PicOps?Action=search&pos=${pos-8}&searchType=${searchType}&params=${params}#bottom" class="button" id="buttonOK">Previous</a> </c:if>
    <c:if test="${pos+1 <=resultSize}"> <a href="PicOps?Action=search&pos=${pos+8}&searchType=${searchType}&params=${params}#bottom" class="button" id="buttonOK">Next</a></c:if>
     
</c:if>
<a name = "bottom"/>
<p></p>