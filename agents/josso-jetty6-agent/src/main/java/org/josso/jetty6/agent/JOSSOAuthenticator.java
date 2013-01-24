package org.josso.jetty6.agent;

import java.io.IOException;
import java.lang.String;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.*;
import org.josso.agent.http.JOSSOSecurityContext;
import org.josso.agent.http.WebAccessControlUtil;
import org.josso.gateway.identity.SSOUser;
import org.josso.servlet.agent.GenericServletSSOAgentFilter;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.UserRealm;

public class JOSSOAuthenticator extends GenericServletSSOAgentFilter implements Authenticator
{
    private static final Log log = LogFactory.getLog(JOSSOAuthenticator.class);

    private static final String JOSSO_AUTH = "JOSSO_AUTH";

    private static final FilterChain filterChain = new DummyFilterChain();

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

        SSOUser ssoUser = null;

        try {
            if (agent == null) {
                context = request.getServletContext();
                context.setAttribute(KEY_SESSION_MAP, new HashMap());
                startup();
            }

            doFilter(request, response, null);

            JOSSOSecurityContext sctx = WebAccessControlUtil.getSecurityContext(request);

            if (sctx != null && sctx.getCurrentPrincipal() != null) {
                ssoUser = sctx.getCurrentPrincipal();
            }

        } catch (ServletException e) {
            throw new IOException("Fatal error performing JOSSO Authentication");
        }

        return ssoUser;
    }
    
    /* ------------------------------------------------------------ */
    public String getAuthMethod()
    {
        return JOSSO_AUTH;
    }

    static class DummyFilterChain implements FilterChain {

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
            // do nothing
        }
    }

}
