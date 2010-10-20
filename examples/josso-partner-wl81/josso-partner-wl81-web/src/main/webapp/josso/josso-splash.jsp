<html>
<body>
<p>
    <%=request.getSession().getAttribute("org.josso.servlet.agent.savedRequest")%>
</p>
<p>
    <a href="<%=request.getSession().getAttribute("org.josso.servlet.agent.savedRequest")%>" >CLICK AFTER SPLASH</a>
</p>
</body>
</html>