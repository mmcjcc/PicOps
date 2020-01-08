<h1>New User Registration</h1>
<hr />
<c:if test="${requestScope.error ==null}"> <%-- display the message as user has not submitted the form yet--%>
    <p>Thank you for your interest in PicOPS!</p><p>Please Enter the requested information below to open an account on PicOps!<br>
    Please note that <strong>all fields are required </strong>, and a valid email address is required to use PicOps.  
    Your email will only be used for the purpose of this site.  <br>
    <p>Once you submit this form, please check your email for instructions on how to complete your registration.  
    <strong>You must complete the registration process within 3 days, so please check your email!</strong></p>
</c:if> 
          
<c:if test="${requestScope.error !=null}"> <%-- we have a signup error, display the message --%>
    <p class = "redtextbig"><strong>Error while attempting to process signup</strong>
    <p class=redtext>${requestScope.error} </p>
</c:if> 



<fieldset class="fieldsignupform"> 
  <legend>Signup</legend>
  <form action="PicOps?Action=signup" method="post">      
    <br />
        <label class="signupform">First Name: </label><input name="nameFirst" type="text" size="25" maxlength="20"class="input-box" /><br />
        <label class="signupform">Last Name: </label><input name="nameLast" type="text" size="25" maxlength="20" class="input-box"/><br />
        <label class="signupform">Email: </label><input name="email" type="text" size="25" maxlength="50" class="input-box" /> <br />
        <label class="signupform">Desired User Name: </label><input name="userName" type="text" size="25" maxlength="20" class="input-box" /> <br />
        <label class="signupform">Password: </label><input name="password" type="password" size="25" maxlength="20" class="input-box"/>  <span style="color:red"> Passwords must be at least 6 characters</span><br />
        <label class="signupform">Confirm Password: </label><input name="password2" type="password" size="25" maxlength="20" class="input-box" /><br />
        <label class="signupform">Secret Question:</label><select name="question" class="input-box">
            <option value="1">City of birth</option>
            <option value="2">Mother's maden name</option>
            <option value="3">Favorite color</option> 
            <option value="4">A word with meaning to you</option> 
        </select><br />
        <label class="signupform">Secret Answer: </label><input name="answer" type="password" size="25" maxlength="50" class="input-box" /><br />
  
        <input name="Submit" type="submit" value="submit" class="submit-button-small"/> <input name="Reset" type="reset" value="reset" class="submit-button-small"/><br />
     </form>
</fieldset>
<p></p>
