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

<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="logic" uri="http://jakarta.apache.org/struts/tags-logic" %>

    <html:errors/>

    <logic:notEmpty name="org.josso.gateway.user">

        <bean:define id="ssoUsername" name="org.josso.gateway.user" property="name" toScope="page"/>
        <bean:define id="ssoSessionId" name="org.josso.gateway.session" property="id" toScope="page"/>

        <div id="authentication-success">

            <div id="subwrapper">

                <div class="main">

                    <h2><bean:message key="sso.title.login.success" arg0="<%=(String)ssoUsername%>"/></h2>

                    <p><bean:message key="sso.text.login.success"  arg0="<%=(String)ssoUsername%>"/></p>

                    <div class="highlight horizontal info">
                        <p><strong><bean:message key="sso.text.ssoSession"  arg0="<%=(String)ssoSessionId%>"/></strong></p>
                        <div class="footer"></div>
                    </div><!-- /highlight -->

                    <html:link forward="info" styleClass="button"><bean:message key="sso.button.details" /></html:link>

                    <div id="user-info" class="clearfix">

                        <div id="col1">

                            <h3 class="arrow"><bean:message key="sso.title.userInformation" /></h3>
                            <ul>
                                  <li><h4><bean:message key="sso.label.username" /></h4><%=ssoUsername%></li>
                                  <li><h4><bean:message key="sso.label.ssoSession" /></h4><%=ssoSessionId%></li>
                            </ul>
                        </div>

                        <div id="col2">
                            <h3 class="arrow"><bean:message key="sso.title.roles" /></h3>

                            <logic:notEmpty name="org.josso.gateway.userRoles">
                                <ul>
                                <logic:iterate name="org.josso.gateway.userRoles" id="role">
                                    <li><h4>&nbsp;</h4><bean:write name="role" property="name"/></li>
                                </logic:iterate>
                                </ul>
                            </logic:notEmpty>

                        </div>

                        <div id="col3">
                            <h3 class="arrow"><bean:message key="sso.title.properties" /></h3>

                            <ul>
                            <logic:iterate name="org.josso.gateway.user" property="properties" id="nvpair">
                                <li><h4><bean:write name="nvpair" property="name"/></h4><bean:write name="nvpair" property="value"/></li>
                            </logic:iterate>
                            </ul>
                        </div>

                        <div id="col4">
                            <h3 class="arrow">Session information</h3>
                            <ul>
                                <li><h4><bean:message key="sso.label.ssoSession.id" /></h4><bean:write name="org.josso.gateway.session" property="id"/></li>
                                <li><h4><bean:message key="sso.label.ssoSession.accessCount" /></h4><bean:write name="org.josso.gateway.session" property="accessCount"/></li>
                                <li><h4><bean:message key="sso.label.ssoSession.lastAccessTime" /></h4><bean:write name="org.josso.gateway.session" property="lastAccessTime"/></li>
                                <li><h4><bean:message key="sso.label.ssoSession.creationTime" /></h4><bean:write name="org.josso.gateway.session" property="creationTime"/></li>
                                <li><h4><bean:message key="sso.label.ssoSession.maxIdleTime" /></h4><bean:write name="org.josso.gateway.session" property="maxInactiveInterval"/></li>
                                <li><h4><bean:message key="sso.label.ssoSession.valid" /></h4><bean:write name="org.josso.gateway.session" property="valid"/></li>
                            </ul>
                        </div>

                    </div> <!-- /user-info -->
                </div><!-- /main -->
            </div><!-- /subwrapper -->
        </div> <!-- /authentication-success -->
    </logic:notEmpty>
