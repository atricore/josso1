<!--#include file='josso.asp'-->
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
						<td colspan="2" align="center">&nbsp;</td>
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
						<td>gwy.endpoint : </td>
						<td><%=josso.getProperty("gwy.endpoint")%></td>
					</tr>
					<tr>
						<td>gwy.login : </td>
						<td><%=josso.getProperty("gwy.login")%></td>
					</tr>
					<tr>
						<td>gwy.logout : </td>
						<td><%=josso.getProperty("gwy.logout")%></td>
					</tr>
					<tr>
						<td>agent.basecode : </td>
						<td><%=josso.getProperty("agent.basecode")%></td>
					</tr>


					<tr>
						<td colspan="2" align="center">&nbsp;</td>
					</tr>
				    <tr>
						<td>init ... </td>
						<td><%=josso.init()%></td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

</body>
</html>