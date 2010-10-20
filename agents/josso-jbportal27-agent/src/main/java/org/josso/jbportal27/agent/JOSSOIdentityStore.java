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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanServerLocator;
import org.josso.auth.BindableCredentialStore;
import org.josso.auth.Credential;
import org.josso.auth.CredentialKey;
import org.josso.auth.CredentialProvider;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.scheme.AuthenticationScheme;
import org.josso.auth.scheme.PasswordCredential;
import org.josso.auth.scheme.UsernameCredential;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.gateway.identity.service.store.IdentityStore;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.josso.gateway.identity.service.store.UserKey;

/**
 * @org.apache.xbean.XBean element="jbportal-identity-store"
 * @author <a href="mailto:sshah@redhat.com">Sohil Shah</a>
 *
 */
public class JOSSOIdentityStore implements IdentityStore, BindableCredentialStore
{
   /**
    * 
    */
   private static final Log logger = LogFactory.getLog(JOSSOIdentityStore.class);
   
   /**
    * 
    */
   private AuthenticationScheme authenticationScheme = null;
   
   /**
    * 
    */
   private JOSSOIdentityService portalIdentityService = null;
   
   
   /**
    * 
    *
    */
   public JOSSOIdentityStore()
   { 
      try
      {
         MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
         this.portalIdentityService = (JOSSOIdentityService)
         MBeanProxy.get(JOSSOIdentityService.class,new ObjectName("portal:service=Module,type=JOSSOIdentityService"),mbeanServer);
      }
      catch(Exception e)
      {
         this.authenticationScheme = null;
         this.portalIdentityService = null;
       
         logger.error(this, e);
         throw new RuntimeException("JOSSOIdentityStore registration failed....");
      }
   }
   //-----IdentityStore implementation--------------------------------------------------------------------------------------------------
   /**
    * 
    */
   public BaseRole[] findRolesByUserKey(UserKey userKey)
         throws SSOIdentityException
   {
      if(this.portalIdentityService == null)
      {
         throw new IllegalStateException("JOSSOIdentityStore not properly registered with the JOSSO system..");
      }
      
      //Get the role information from the Portal Identity System
      String[] userRoles = this.portalIdentityService.getUserRoles(userKey.toString());
      
      //Map the Portal Identity information to JOSSO Identity information
      BaseRole[] roles = new BaseRole[userRoles.length];     
      for(int i=0; i<userRoles.length; i++)
      {
         roles[i] = new BaseRoleImpl(userRoles[i]);
      }
      
      
      return roles;
   }

   /**
    * 
    */
   public BaseUser loadUser(UserKey userKey) throws NoSuchUserException,
         SSOIdentityException
   {   
      if(this.portalIdentityService == null)
      {
         throw new IllegalStateException("JOSSOIdentityStore not properly registered with the JOSSO system..");
      }
      
      //Map the Portal Identity to JOSSO Identity
      BaseUser user = new BaseUserImpl();
      user.setName(userKey.toString());
      user.addProperty("password", "");
      
      return user;
   }

   /**
    * 
    */
   public boolean userExists(UserKey userKey) throws SSOIdentityException
   {
      if(this.portalIdentityService == null)
      {
         throw new IllegalStateException("JOSSOIdentityStore not properly registered with the JOSSO system..");
      }
      
      return this.portalIdentityService.exists(userKey.toString());
   }
   //---------BindableCredentialStore implementation---------------------------------------------------------------------------------------------
   /**
    * 
    */
   public Credential[] loadCredentials(CredentialKey credentialKey) throws SSOIdentityException
   {
      if(this.portalIdentityService == null)
      {
         throw new IllegalStateException("JOSSOIdentityStore not properly registered with the JOSSO system..");
      }
      
      //Get the User corresponding to this credentialKey
      BaseUser user = this.loadUser((SimpleUserKey)credentialKey);
      SSONameValuePair[] properties = user.getProperties();
      String password = properties[0].getValue();
      
      return new Credential[]{new UsernameCredential(user.getName()), new PasswordCredential(password)};
   }
   
   public Credential[] loadCredentials(CredentialKey credentialKey, CredentialProvider credentialProvider) throws SSOIdentityException {
	   return loadCredentials(credentialKey);
   }   
   
   /**
    * 
    */
   public boolean bind(String username, String password) throws SSOAuthenticationException {
      return this.portalIdentityService.authenticate(username, password);
   }
   
   
   /**
    * 
    */
   public void setAuthenticationScheme(AuthenticationScheme authenticationScheme) {
      if(this.portalIdentityService == null)
      {
         throw new IllegalStateException("JOSSOIdentityStore not properly registered with the JOSSO system..");
      }
      
      this.authenticationScheme = authenticationScheme;
   }

    public String loadUID(CredentialKey key, CredentialProvider cp) throws SSOIdentityException {
        throw new UnsupportedOperationException("Not implemented for this agent!");
    }
}



