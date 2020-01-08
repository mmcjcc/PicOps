<h1> PicOps Login </h1> <hr />
<c:if test="${activated == true}"> <%-- someone completed activation --%>
<p class = "redtextbig"><strong>Thank you for activating your account, you can now sign in using the form below!</strong>
<p class=redtext> </p>
</c:if> 
<c:if test="${error != null}"> <%-- we have a signin error, display the message --%>
<p class = "redtextbig"><strong>${error}</strong>
<p class=redtext> </p><p class = "redtext">Please try again:</p>
</c:if> 
<p></p>
<fieldset>
        <legend>Login</legend>
        <form action="PicOps?Action=login" method="post">
            <label for="name">User Name:</label> <input name="userName" type="text"  size="20" maxlength="20" class="input-box" /><br />
            <label for="e-mail">Password:</label> <input name="password" size="20" maxlength="20" type="password" class="input-box" /><br />
            <input type="submit" value="Login" class="submit-button" />
        </form>
    </fieldset> 
