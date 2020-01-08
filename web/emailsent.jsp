<c:choose>
    <c:when test ="${status == 'emailSent'}">
     <h2> Your registration is almost complete.... </h2>
     <p class="redtext"> Pleae check the inbox for the email address you used during signup for instructions on completing your registration!</p>
    </c:when>
    <c:otherwise>
       <jsp:forward page="/PicOps?Action="/> 
    </c:otherwise>
</c:choose>
