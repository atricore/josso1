<%@ include file="/WEB-INF/jsp/include.jsp" %>

<html>
<head><title>Permission Deleted</title></head>
<body>
<h1>Permission Deleted</h1>
<P>
<code>
<c:out value="${model.contact}"/>
</code>
<P>
<code>
<c:out value="${model.sid}"/>
</code>
<code>
<c:out value="${model.permission}"/>
</code>
<p><a href="<c:url value="index.htm"/>">Manage</a>
</body>
</html>
