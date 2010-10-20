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

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>

        <div id="authentication">

                <html:errors/>

                <div id="subwrapper">

                    <div class="main">
                        <h2><bean:message key="sso.title.userLogin"/></h2>

                        <p><bean:message key="sso.text.userLogin"/></p>


                        <html:form action="/signon/usernamePasswordLogin" focus="josso_username" >

                            <fieldset>
                                <html:hidden property="josso_cmd" value="login"/>
                                <html:hidden property="josso_back_to"/>

                                <div><label for="username"><bean:message key="sso.label.username"/> </label> <html:text styleClass="text" property="josso_username" />
                                </div>
                                <div><label for="password"><bean:message key="sso.label.password"/> </label> <html:password styleClass="text error" property="josso_password" /></div>
                                <div class="indent"><html:checkbox property="josso_rememberme" styleClass="checkbox"/><bean:message key="sso.label.rememberme"/></div>
                            </fieldset>

                            <div><input class="button indent" type="submit" value="Login"/></div>
                        </html:form>
                        <p class="indent"><a href="<%=request.getContextPath()%>/selfservices/lostpassword/lostPassword.do?josso_cmd=lostPwd"><bean:message key="sso.label.forgotPassword"/></a></p>

                        <div class="highlight">
                            <h3 class="help"><bean:message key="sso.title.help"/></h3>

                            <p><bean:message key="sso.text.login.help"/>.</p>

                            <div class="footer"></div>

                        </div>
                        <!-- /highlight -->

                    </div>
                    <!-- /main -->
                </div>

            </div> <!-- /authentication -->
        