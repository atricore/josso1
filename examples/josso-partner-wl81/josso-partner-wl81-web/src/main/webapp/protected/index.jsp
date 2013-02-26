<%--
  ~ JOSSO: Java Open Single Sign-On
  ~
  ~ Copyright 2004-2009, Atricore, Inc.
  ~
  ~ This is free software; you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as
  ~ published by the Free Software Foundation; either version 2.1 of
  ~ the License, or (at your option) any later version.
  ~
  ~ This software is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ Lesser General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with this software; if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  ~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  ~
  --%>
<%@ page import="java.util.Enumeration" %>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="es" lang="es">

<head>

      <title>JOSSO - Java Open Single SignOn</title><!-- Edit -->

      <meta name="Title" content="Atricore, Inc" />
      <meta name="Author" content="Nicolas Calabrese,Sebastian Gonzalez Oyuela" />
      <meta name="Keywords" content="JOSSO Sample Partner Application" />
      <meta name="Description" content="This is a JOSSO partner application." />

      <meta name="Robots" content="index,follow" />
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

    <link href="<%=request.getContextPath()%>/resources/css/screen.css" rel="stylesheet" type="text/css" media="screen, projector"/>

    <!--[if IE 6]><link href="<%=request.getContextPath()%>/resources/css/ie6.css" rel="stylesheet" type="text/css" media="screen, projector" /><![endif]-->
    <!--[if IE 7]><link href="<%=request.getContextPath()%>/resources/css/ie7.css" rel="stylesheet" type="text/css" media="screen, projector" /><![endif]-->

</head>


<body>
      <div><p class="alert browser-support">Using a modern browser that supports web standards ensures that the site's full visual experience is available.  Consider <a href="http://www.opera.com/products/desktop/">upgrading your browser</a> if you are using an older technology.</p></div>

<div id="wrapper">

<!-- PAGE HEADER  -->

      <div id="header">

            <h1> <!-- Logo JOSSO-->
                  <a href="http://www.josso.org" title="Click here to go to the homepage">
                        <img src="<%=request.getContextPath()%>/resources/img/content/josso-logo.png" alt="Java Open Single SignOn" width="372" height="48" />
                  </a>
            </h1> <!-- /Logo JOSSO -->

            <h2> <!-- Logo Atricore -->
                  <a href="http://www.atricore.com">
                        <img src="<%=request.getContextPath()%>/resources/img/content/atricore-logo.gif" alt="Atricore, the company behind JOSSO" width="254" height="66" />
                  </a>
            </h2> <!-- /Logo Atricore -->


      </div>

<!-- PAGE CONTENT  -->

        <div id="content" class="clearfix">
            <div id="partner-protected">
                <div id="subwrapper">
                    <div class="main">
                        <h2>Welcome!</h2>

                        <p>This is a very simple JOSSO partner application, you're accessing a protected web resource. You can find some tips to use in your own web apps!</p>

                        <div class="highlight horizontal info">
                              <p><strong>Username: <%=request.getRemoteUser()%></strong> ( Retrieved from <code>request.getRemoteUser()</code> )</p>
                              <div class="footer"></div>
                        </div><!-- /highlight -->

                        <a href="<%=request.getContextPath()%>/josso-wls/josso_logout.jsp" class="button">Logout</a>

                        <div id="login-options" class="clearfix">

                            <div id="col1">
                                  <h3 class="arrow">Login URL</h3>
                                  <p>The login URL is <a href="<%=request.getContextPath()%>/josso-wls/josso_user_login.jsp"><%=request.getContextPath()%>/josso-wls/josso_user_login.jsp/</a>. <br/>Do not forget the trailing '/'</p>
                            </div>

                            <div id="col2">
                                  <h3 class="arrow">Logout URL</h3>
                                  <p>The logout URL is <a href="<%=request.getContextPath()%>/josso-wls/josso_logout.jsp"><%=request.getContextPath()%>/josso-wls/josso_logout.jsp/</a>. <br/>Do not forget the trailing '/'</p>
                            </div>

                            <div id="col3">
                                <h3 class="arrow">Http Headers</h3>

                                <%
                                 out.println("<ul>");
                                 java.util.Enumeration names = request.getHeaderNames();
                                 while (names.hasMoreElements()) {
                                   String name = (String) names.nextElement();
                                   Enumeration values = request.getHeaders(name);

                                   while (values.hasMoreElements()) {
                                       String value = (String)values.nextElement();

                                       out.println(" <li><strong>" + name + "=</strong>" + value +"</li>");
                                   }

                                 }
                                 out.println("</ul>");
                                 %>

                            </div>

                        </div> <!-- /login-options -->

                  </div><!-- /main -->

              </div> <!-- /subwrapper -->

          </div><!-- /partner-protected -->

        </div><!-- /content -->


<!-- PAGE FOOTER  -->

      <div id="footer">
            <p>Copyright &copy; 2004-2013. Atricore, Inc.</p>
      </div>


</div>

</body>
</html>





