package org.josso.jetty6.agent;

import java.io.IOException;
import java.lang.String;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.UserRealm;

public class JOSSOAuthenticator implements Authenticator
{

    private static final String JOSSO_AUTH = "JOSSO_AUTH";
    
    /* ------------------------------------------------------------ */
    public JOSSOAuthenticator()
    {
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return UserPrinciple if authenticated or null if not. If
     * Authentication fails, then the authenticator may have committed
     * the response as an auth challenge or redirect.
     * @exception IOException 
     */
    public Principal authenticate(UserRealm realm,
            String pathInContext,
            Request request,
            Response response)
        throws IOException
    {
        return null;
    }
    
    /* ------------------------------------------------------------ */
    public String getAuthMethod()
    {
        return JOSSO_AUTH;
    }

}
