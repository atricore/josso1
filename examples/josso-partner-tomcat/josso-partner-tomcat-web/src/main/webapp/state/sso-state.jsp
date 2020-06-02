<%@ page import="java.util.Set" %><%
    String user = request.getRemoteUser();
    String oldUser = (String) request.getSession().getAttribute("old_user");

    if (user == null ? oldUser == null : user.equals(oldUser)) {
        out.println("unchanged");
    } else {
        request.getSession().setAttribute("old_user", user);
        out.println("changed");
    }
%>
