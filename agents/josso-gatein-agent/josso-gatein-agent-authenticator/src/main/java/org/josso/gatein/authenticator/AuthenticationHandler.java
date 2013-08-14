/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
*/
package org.josso.gatein.authenticator;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.security.auth.login.LoginException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;

import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;

import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This is a RESTful component that is invoked by central SSO servers like CAS server, JOSSO server etc, to invoke
 * Gatein authentication related queries during their own "Authentication process"
 * 
 * 
 * @author <a href="mailto:sshah@redhat.com">Sohil Shah</a>
 */
@Path("/sso/authcallback")
public class AuthenticationHandler implements ResourceContainer
{
	 private static Logger log = Logger.getLogger(AuthenticationHandler.class);
	
	 @GET
	 @Path("/auth/{1}/{2}")
   @Produces(
   {MediaType.TEXT_PLAIN})
   public String authenticate(@PathParam("1") String encodedUsername, @PathParam("2") String encodedPassword)
   {
		 try
		 {
              String username = new String(Base64.decodeBase64(encodedUsername.getBytes()));
              String password = new String(Base64.decodeBase64(encodedPassword.getBytes()));

			  log.debug("---------------------------------------");
			  log.debug("Username: "+ username);
			  log.debug("Password: "+ password);
			  
			  ExoContainer container = this.getContainer();
			  Authenticator authenticator = (Authenticator) getContainer().getComponentInstanceOfType(Authenticator.class);
			  			  
			  Credential[] credentials = new Credential[] { new UsernameCredential(username),
          new PasswordCredential(password) };			  			  

			  try
			  {
			  	authenticator.validateUser(credentials);
			  	return ""+Boolean.TRUE;
			  }
			  catch(LoginException le)
			  {
			  	return ""+Boolean.FALSE;
			  }			  			  			  			  
		 }
		 catch(Exception e)
		 {
			 log.error(this, e);
			 throw new RuntimeException(e);
		 }
   }
	 
	 private ExoContainer getContainer() throws Exception 
	 {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container instanceof RootContainer) 
    {
      container = RootContainer.getInstance().getPortalContainer("portal");
    }
    return container;
  }
}
