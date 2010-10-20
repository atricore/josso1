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

package org.josso.jb42.agent;

import javax.security.auth.Subject;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;

/**
 * A PrivilegedAction implementation for setting the SecurityAssociation
 * principal and credential for JBoss.
 * <p>
 * This class is used by the JBossCatalinaRealm class to set the authenticated Principal
 * using the SetPrincipalInfoAction PrivilegedAction class.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: JBossSecurityAssociationActions.java 974 2009-01-14 00:39:45Z sgonzalez $
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
         org.jboss.security.SecurityAssociation.pushSubjectContext(subject, principal, credential);
         credential = null;
         principal = null;
         subject = null;
         return null;
      }
   }

    static void setPrincipalInfo(Principal principal, Object credential, Subject subject)
    {
       SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential, subject);
       AccessController.doPrivileged(action);
    }

}
