<!--#include file='josso-asp/josso.asp'-->
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
						<td><b>SSO Session:</b></td>
						<td><%=getJOSSOToken() %></td>
					</tr>
					<%if getJOSSOToken = "" then %>
                	<tr>
						<td colspan="2" align="center"><b>NOT PRESENT</b></td>
					</tr>
					<%end if %>
					
					<tr>
						<td colspan="2" align="center">&nbsp;</td>
					</tr>
					<tr>
						<td colspan="2" align="center">This is a sample ASP public page that uses JOSSO as Single Sing-On</td>
					</tr>
					<tr>
						<td colspan="2" align="center">&nbsp;</td>
					</tr>
					<tr>
						<td colspan="2" align="center">Check the Sample <a href="sample-public.asp">Public ASP</a></b></td>
					</tr>
					<tr>
						<td colspan="2" align="center">Check the Sample <a href="sample-protected.asp">Protected ASP</a></b></td>
					</tr>
					<tr>
						<td colspan="2" align="center">Check the Sample <a href="sample-user.asp">User ASP</a></b></td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	
</body>
</html>