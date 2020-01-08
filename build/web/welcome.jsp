<h1 align = "left">Welcome to PicOps</h1> <hr />
<c:if test="${sessionScope.loggedIn =='true' && sessionScope.userBean !=null}">
<p class="navytext">Thanks for using PicOps! <br /><br />Please feel free to click the contact link with any questions or comments regarding PicOps.<br />Remember, if you  want to share your public albums, use this URL: 
<br /><a  class = "redlink" href= "${webaddress}/PicOps/PicOps?Action=displaypubalbums&user=${userBean.userName}">http://picops.ezjcc.com/PicOps/PicOps?Action=displaypubalbums&user=${userBean.userName}</a>
<br /><br /> Enjoy some random albums!<br /><br />Thanks,<br />PicOps "Team"</p>
</c:if>
<c:if test="${sessionScope.loggedIn !='true' || sessionScope.userBean ==null}"> <%-- session expired or user never loggedin--%>
<p align="left" class = "redtext">Hello and thank you for checking out PicOps, your Online Photo System! </p>
<p class="navytext">With PicOps, you can create online photo albums and share them with anyone on the web for free.  If you already have an account, please sign up below.  
If you would like to become a user and take advantage of some of PicOps's super cool features then click on the link to sign up for 
an account!</p>
<p></p>
    <fieldset>
        <legend>Login</legend>
        <form action="PicOps?Action=login" method="post">
            <label>User Name:</label> <input name="userName" type="text"  size="20" maxlength="20" class="input-box" /><br />
            <label>Password:</label> <input name="password" size="20" maxlength="20" type="password" class="input-box" /><br />
            <input type="submit" value="Login" class="submit-button" />
        </form>
    </fieldset> 
</c:if>
<p></p><p></p><p></p>
<c:if test="${pubAlbums != null}">
    <table class="albums" cellpadding = "10" cellspacing="10" border="1" width="700" align="center" valign = "bottom">
        <tr>
        <c:set var="i" value="1" scope ="page"/>
        <c:forEach items="${pubAlbums}" var = "album"> <%-- we have some albums, display icons for them as links to full album--%>

        <td> <a  href="PicOps?Action=displayalbum&id=${album.id}" class= "picborder"> <img src =PicOps?Action=displaythumbnail&id=${album.icon} alt="album icon for ${album.title}"></a>
            
        <c:if test="${i % 3 == 0}">
            </tr><tr>
        </c:if> 
            <c:set var="i" value="${i +1}" scope ="page"/>  
            </c:forEach>
        </tr>
    </table>
    <br />
</c:if>