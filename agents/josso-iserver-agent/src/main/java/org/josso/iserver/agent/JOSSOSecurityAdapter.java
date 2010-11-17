package org.josso.iserver.agent;

import com.actuate.iportal.security.iPortalSecurityAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class JOSSOSecurityAdapter extends iPortalSecurityAdapter {
    private String username;
    private String passwd;

    /**
     * One agent instance for all applications.
     */
    private HttpSSOAgent _agent;

    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(JOSSOSecurityAdapter.class);

    public JOSSOSecurityAdapter() {
        super();
    }

    private void initAgent() {
        Lookup lookup = Lookup.getInstance();
        lookup.init("josso-agent-config.xml"); // For spring compatibility ...

        // We need at least an abstract SSO Agent
        try {
            _agent = (HttpSSOAgent) lookup.lookupSSOAgent();
            if (log.isDebugEnabled())
                _agent.setDebug(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Cookie getJossoCookie(HttpServletRequest hreq) {
        Cookie cookie = null;
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }

        return cookie;
    }

    public boolean authenticate(HttpServletRequest hReq) {
        initAgent();

        try {
            HttpSession hSession = hReq.getSession();

            Cookie jCookie = getJossoCookie(hReq);

            if (jCookie == null || jCookie.getValue().equals("-"))
                return false;
            // token is jossoSessionId
            String token = "";
            token = jCookie.getValue();

            SSOIdentityManagerService im = Lookup.getInstance().lookupSSOAgent().getSSOIdentityManager();
            SSOUser ssoUser = im.findUserInSession(token, token);
            if (ssoUser != null) {
                username = ssoUser.getName();
                passwd = "";
                return true;
            }

        } catch (NoSuchUserException e) {
            e.printStackTrace();
            return false;
        } catch (SSOIdentityException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    @Override
    public byte[] getExtendedCredentials() {
        return super.getExtendedCredentials();
    }

    @Override
    public String getPassword() {
        return passwd;
    }

    @Override
    public String getRepositoryType() {
        return super.getRepositoryType();
    }

    @Override
    public String getServerUrl() {
        return super.getServerUrl();
    }

    @Override
    public String getUserHomeFolder() {
        return super.getUserHomeFolder();
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getVolume() {
        return super.getVolume();
    }

    @Override
    public boolean isEnterprise() {
        return super.isEnterprise();
    }
}
