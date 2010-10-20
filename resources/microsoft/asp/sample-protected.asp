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
' Because we included josso-protected.asp, we're sure that a user is present.

set jossoUser = jossoCurrentUser()

' If the user has not been authenticated, request a login ...
if jossoUser is nothing then
    jossoRequestLogin()
end if

dim jossoSession
jossoSession = getJOSSOToken()

' Find current JOSSO User's roles
' Because we included josso-protected.asp, we're sure that a user is present.
dim jossoRoles
set jossoRoles = josso.getUserRoles(jossoSession)

' If the user does not have "role1", send a forbbiden response ...
if not josso.isUserInRole(jossoSession, "role1") then
    Response.Status = "403 Acceso prohibido"
    Response.Write Response.Status
    Response.End
end if


' Find current JOSSO User's properties
' Because we included josso-protected.asp, we're sure that a user is present.
dim jossoProperties
set jossoProperties = josso.getUserProperties(jossoSession)


' We could also restrict access based on user properties ...



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
						<td align="right">Session :</td><td><%=jossoSession%></td>
					</tr>
					<tr>
					 	<td colspan="2" align="center"><b>Properties (optional)</b></td>
					</tr>
<%					dim i
					For i = 0 to ( jossoProperties.count() - 1  ) %>
					<tr>
						<td align="right" ><%=jossoProperties.getName(i)%>&nbsp;:</td><td><%=jossoProperties.getValue(i)%></td>
					</tr>
						
<%					Next %>						
					<tr>
						<td colspan="2" align="center"><b>Roles (optional)</b></td>
					</tr>
					 <% dim j, jossoRole
						For j = 0 to ( jossoRoles.count() - 1  )
						    set jossoRole = jossoRoles.getRole(j) %>
							<tr>
								<td align="center" colspan="2"><%=jossoRole.getName()%></td>
							</tr>
<%   					Next %>
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



