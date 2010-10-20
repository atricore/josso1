/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.josso.jbportal27.agent;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/*
 * Created on May 23, 2007
 * 
 * @author <a href="mailto:sshah@redhat.com">Sohil Shah</a>
 */
public class JOSSOLogoutValve extends ValveBase
{
	private static String JOSSO_LOGOUT_URI = "/josso_logout/";
   /**
    * 
    */
   public void invoke(Request request, Response response) throws IOException,
         ServletException
   {
      HttpServletRequest httpRequest = (HttpServletRequest) request;   
      request.setAttribute("ssoEnabled", "true");
            
      Cookie jossoPortalCookie = this.findJOSSOPortalLogoutCookie(httpRequest);
      if(jossoPortalCookie != null)
      {
         String referer = jossoPortalCookie.getValue();
         
         if(referer != null && referer.trim().length() > 0)
         {
            //Delete this cookie
            jossoPortalCookie = new Cookie("JOSSO_PORTAL_LOGOUT", "");
            jossoPortalCookie.setMaxAge(0); //setting the value to 0 should delete this cookie from the browser
            response.addCookie(jossoPortalCookie);
            
            //This form of redirect is needed instead of sendRedirect
            //otherwise the JBOSS_PORTAL_LOGOUT cookie cleanup does not happen
            StringBuffer buffer = new StringBuffer();
            buffer.append("<html>"+"\n");
            buffer.append("<head>"+"\n");
            buffer.append("</head>"+"\n");
            buffer.append("<body onload=\"setTimeout('document.form1.submit()',1000);\">"+"\n");
            buffer.append("<form name=\"form1\" action=\""+getRequestURL(httpRequest)+"\" method=\"post\">"+"\n");            
            buffer.append("</form>"+"\n");
            buffer.append("</body>"+"\n");
            buffer.append("</html>"+"\n");
            
            response.getOutputStream().write(buffer.toString().getBytes());
            response.getOutputStream().flush();
            
            return;
         }
      }
      
      // continue processing the request
      this.getNext().invoke(request, response);
      
      if(request.getAttribute("org.jboss.portal.logout") != null)
      {         
         String jossoLogout = httpRequest.getContextPath() + JOSSO_LOGOUT_URI;                  
         
         Cookie cookie = new Cookie("JOSSO_PORTAL_LOGOUT",httpRequest.getHeader("Referer"));
         cookie.setMaxAge(-1); //setting the value so that cookie expires when browser is closed
         response.addCookie(cookie);
         
         response.sendRedirect(jossoLogout);
      }            
   }   
   
   /**
    * 
    * @param request
    * @return
    */
   private Cookie findJOSSOPortalLogoutCookie(HttpServletRequest request)
   {
      Cookie cookie = null;
      
      Cookie[] cookies = request.getCookies();
      if(cookies != null)
      {
         for(int i=0; i<cookies.length; i++)
         {
            Cookie cour = cookies[i];
            
            if(cour.getName().equals("JOSSO_PORTAL_LOGOUT"))
            {
               cookie = cour;
               break;
            }
         }
      }
      
      return cookie;
   }
   
   private String getRequestURL(HttpServletRequest request) {
       StringBuffer sb = new StringBuffer(request.getRequestURI());
       if (request.getQueryString() != null) {
           String q = request.getQueryString();
           if (!q.startsWith("?"))
               sb.append('?');
           sb.append(q);
       }
       return sb.toString();
   }
}
