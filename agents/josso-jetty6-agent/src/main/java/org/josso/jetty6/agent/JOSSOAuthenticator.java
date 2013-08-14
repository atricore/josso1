package org.josso.jetty6.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.*;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.agent.http.WebAccessControlUtil;
import org.josso.servlet.agent.GenericServletLocalSession;
import org.josso.servlet.agent.GenericServletSSOAgentRequest;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.Context;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JOSSOAuthenticator implements Authenticator
{
    private static final Log log = LogFactory.getLog(JOSSOAuthenticator.class);

    public static final String KEY_SESSION_MAP = "org.josso.jetty6.agent.sessionMap";
    public static final String LAZY_STARTUP ="lazy";
    
    /**
     * One agent instance for all applications.
     */
    protected HttpSSOAgent agent;

    protected void init() throws IOException {

        try {

            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-agent-config.xml"); // For spring compatibility ...

            // We need at least an abstract SSO Agent
            agent = (HttpSSOAgent) lookup.lookupSSOAgent();
            if (log.isDebugEnabled())
                agent.setDebug(1);
            agent.start();

        } catch (Exception e) {
            throw new IOException("Error starting SSO Agent : " + e.getMessage(), e);
        }
        

    }

    public Principal authenticate(UserRealm userRealm, String s, Request request, Response response) throws IOException {
        init();

        HttpServletRequest hreq =
                (HttpServletRequest) request;
        HttpServletResponse hres =
                (HttpServletResponse) response;

        Request baseRequest=(request instanceof Request)?(Request)request:null;

        String contextPath = getContextPath(request);
    
        log.debug("Request is " + request);
        log.debug("Response is " + response);
        log.debug("Session is " + request.getSession(true));
     
        return null;
    }

    public String getAuthMethod() {
        return HttpServletRequest.FORM_AUTH;
    }

    private String getContextPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        String contextPath = null;
        
        if (!path.substring(1).contains("/")) {
            contextPath = path;
        } else {
            contextPath = path.substring(0, path.substring(1).indexOf("/") + 1);
        }

        return contextPath;
    }
    
}
