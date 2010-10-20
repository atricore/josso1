/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.jb5.agent;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;

/**
 * A PrivilegedAction implementation for setting the SecurityAssociation
 * principal and credential for JBoss.
 * <p>
 * This class is used by the JOSSOJASPIAuthenticator class to set the authenticated Principal
 * using the SetPrincipalInfoAction PrivilegedAction class.
 */
class JBossSecurityAssociationActions {

    private static class SetPrincipalInfoAction implements PrivilegedAction
   {
      Principal principal;
      Object credential;
      Subject subject;
      SetPrincipalInfoAction(Principal principal, Object credential, Subject subject)
      {
         this.principal = principal;
         this.credential = credential;
         this.subject = subject;
      }

      public Object run()
      {
    	  SecurityContext sc = getSecurityContext();
          if(sc == null)
             throw new IllegalStateException("Security Context has not been set");
          
          sc.getUtil().createSubjectInfo(principal, credential, subject);
         //SecurityAssociation.pushSubjectContext(subject, principal, credential); 
         //SecurityAssociation.pushSubjectContext(subject, principal, credential);
         //SecurityAssociation.setSubject(subject);
         //SecurityAssociation.setPrincipal(principal);
         //SecurityAssociation.setCredential(credential);
         credential = null;
         principal = null;
         subject = null;
         return null;
      }
   }

    private static class GetSubjectAction implements PrivilegedAction
    {
       static PrivilegedAction ACTION = new GetSubjectAction();
       public Object run()
       {
          //Subject subject = SecurityAssociation.getSubject();
          SecurityContext sc = getSecurityContext();
          if(sc == null)
             throw new IllegalStateException("Security Context is null");
          return sc.getUtil().getSubject(); 
       }
    }
    
    static SecurityContext getSecurityContext()
    {
       return (SecurityContext)AccessController.doPrivileged(new PrivilegedAction()
       { 
          public Object run()
          {
             return SecurityContextAssociation.getSecurityContext(); 
          }
        }); 
    }
    
    static void setPrincipalInfo(Principal principal, Object credential, Subject subject)
    {
       SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential, subject);
       AccessController.doPrivileged(action);
    }

    static Subject getSubject()
    {
       Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
       return subject;
    }
}
