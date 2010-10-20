<%
' ------------------------------------------------------------------------
' This include will ensure that a user is always authenticated ,
' if not the user will be redirected to the login page.
'  After a successful login, he'll be redirected back to this page.
' ------------------------------------------------------------------------
%>
<!--#include file='josso-asp/josso.asp'-->
<%

' Find current JOSSO User for current JOSSO Token
dim jossoUser

set jossoUser = jossoCurrentUser()

' Check if a user is present otherwise request for authentication
if jossoUser is nothing then
    jossoRequestLogin()
end if
%>
<html>
<body>

    <table width="100%" cellpadding="0" cellspacing="0" border="0" >
        <tr>
            <td align="center" class="label">
                <table cellpadding="0" cellspacing="3" border="0" >
					<tr>
						<td colspan="2" align="center"><b><%=josso.getVersion()%></b></td>
					</tr>
					<tr>
						<td colspan="2" align="center">Protected ASP</td>
					</tr>
					<tr>
						<td colspan="2" align="center">&nbsp;</td>
					</tr>
					<tr>
						<td colspan="2" align="center">This is a sample ASP protected page that uses JOSSO as Single Sing-On</td>
					</tr>
					<tr>
						<td colspan="2" align="center">&nbsp;</td>
					</tr>
					<tr>
						<td colspan="2" align="center"><b>User Information</b></td>
					</tr>
					<tr>
						<td align="right">Name :</td><td><%=jossoUser.getName()%></td>
					</tr>
					<tr>
					 	<td colspan="2" align="center"><b>Options</b></td>
					</tr>
					<tr>
					 	<td colspan="2" align="center"><b><a href="<%=jossoCreateLogoutUrl()%>">logout</a></b></td>
					</tr>

                </table>

            </td>
        </tr>
    </table>
</body>
</html>



