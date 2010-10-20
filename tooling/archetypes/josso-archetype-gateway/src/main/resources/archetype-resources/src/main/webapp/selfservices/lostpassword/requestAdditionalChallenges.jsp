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

    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td align="center"><html:errors/></td>
        </tr>
    </table>

    <html:form action="/selfservices/lostpassword/requestAdditionalChallenges" focus="email" >

    <table width="100%" cellpadding="0" cellspacing="0">
		<tr>
            <td align="center" valign="middle" >
                <table cellspacing="0" class="login-form">
                    <tbody>
                        <tr>
                            <td align="center"><bean:message key="sso.label.secretQuestion"/></td>
                            <td><html:password property="secretAnswer" /><br></td>
                        </tr>
                        <tr>
                            <td colspan="2" align="center"><input type="submit" value="Reset Password" ></td>
                        </tr>
                    </tbody>
                </table>
            </td>
        </tr>
	<table>
    </html:form>
