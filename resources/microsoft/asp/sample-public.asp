<!--#include file='josso-asp/josso.asp'-->
    <table width="100%" cellpadding="0" cellspacing="0" border="0" >
        <tr>
            <td align="center" class="label">
                <table cellpadding="0" cellspacing="3" border="0" >
					<tr>
						<td colspan="2" align="center"><b><%=josso.getVersion()%></b></td>
					</tr>
					<tr>
						<td colspan="2" align="center">Public ASP</b></td>
					</tr>
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
					 	<td colspan="2" align="center"><b>Options</b></td>
					</tr>
					<tr>
					 	<td colspan="2" align="center"><b><a href="<%=jossoCreateLogoutUrl()%>">logout</a></b></td>
					</tr>
					<tr>
					 	<td colspan="2" align="center"><b><a href="<%=jossoCreateLoginUrl()%>">login</a></b></td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
