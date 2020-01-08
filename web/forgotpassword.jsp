<h1>Forgot Password</h1>
<hr />
<c:if test="${error ==null}"> <%-- display the message as user has not submitted the form yet--%>
    <p>Select the secret question that you used during signup and enter the answer that you specified.</p>
</c:if> 

<c:if test="${error !=null}"> <%-- we have a signup error, display the message --%>
    <p class=redtext>${error} </p>
</c:if> 

    <p></p>
    <fieldset class="fieldforgotform"> 
    <legend>Reset Password</legend>
        <form action="PicOps?Action=forgot" method="post">
           <br />
            <label class="forgotform">User Name:</label><input name="userName" type="text" size="20" maxlength="20" class="input-box"/> <br />
            <label class="forgotform">Secret Question:</label><select name="question" class="input-box"><br />
                <option value="1">City of birth</option>
                <option value="2">Mother's maden name</option>
                <option value="3">Favorite color</option>
                <option value="4">A word with meaning to you</option>
            </select><br />
            <label class="forgotform"> Secret Answer:</label><input name="answer" type="password" size="20" maxlength="50" class="input-box"/><br />
            <label class="forgotform">New Password: </label><input name="newpassword" type="password" size="20" maxlength="20" class="input-box"/><br />
            <label class="forgotform"> Confirm New Password: </label><input name="newpassword2" type="password" size="20" maxlength="20"class="input-box" /> 
           <br /> <input name="Submit" type="submit" value="submit" class="submit-button-small" /><input name="Reset" type="reset" value="reset" class="submit-button-small"/>
        </form>
    </fieldset>
