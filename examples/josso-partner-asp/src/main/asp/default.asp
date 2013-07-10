<html>
<head>
    <title>JOSSO ASP Partner appliaction using ISAPI Agent</title>
</head>
<body>

    <p>This is a sample JOSSO ASP Parnter application using JOSSO ISAPI Agent.</p>

    <table width="100%" cellpadding="0" cellspacing="0" border="0" >
        <tr>
            <td align="center" class="label" colspan="2">

                <table cellpadding="0" cellspacing="3" border="0" >

                	<tr>
						<td colspan="2" align="center"><b>Hello, <%=Request.ServerVariables("HTTP_JOSSO_USER") %>!</b></td>
					</tr>
                    <%
                    for each varName in Request.ServerVariables
                        If inStr(varName, "HTTP_JOSSO") then
                            response.write("<tr><td>" & varName & "<td/><td>"& Request.ServerVariables(varName) &"</td></tr>")
                        end if
                    next
                    %>
				</table>

			</td>
		</tr>
	</table>
</body>
</html>
