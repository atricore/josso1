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
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>


    <html:errors/>

    <div id="lost-password">

        <div id="subwrapper">

            <div class="main">
                <h2><bean:message key="sso.title.lostPassword"/></h2>

                <p><bean:message key="sso.text.lostPassword"/></p>

                <html:form action="/selfservices/lostpassword/processChallenges" focus="email" >
                    <div><label for="email"><bean:message key="sso.label.email"/></label> <html:text styleClass="text" property="email" /></div>
                    <div><input class="button medium" type="submit" value="Reset password"/></div>
                </html:form>

                <p class="note"><bean:message key="sso.text.buttonOnlyOnce"/></p>

                <div class="highlight">
                    <h3 class="help"><bean:message key="sso.title.help"/></h3>
                    <p><bean:message key="sso.text.lostPassword.help"/></p>
                    <div class="footer"></div>
                </div><!-- /highlight -->

            </div><!-- /main -->

        </div><!-- /subwrapper -->


    </div>
