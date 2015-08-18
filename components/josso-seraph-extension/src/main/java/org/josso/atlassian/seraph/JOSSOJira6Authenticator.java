package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.login.JiraSeraphAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.config.SecurityConfig;
import org.apache.log4j.Logger;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Map;

public class JOSSOJira6Authenticator extends JiraSeraphAuthenticator {

    private static final Logger logger = Logger.getLogger(JOSSOJira6Authenticator.class);

    private CrowdDirectorySelectorStrategy dirSelector;

    private String lookupCrowdDirStrategyType;

    private Map<String, String> params;

    @Override
    public void init(Map<String, String> params, SecurityConfig config) {
        super.init(params, config);
        this.params = params;
        lookupCrowdDirStrategyType = params.get("directory.lookup.strategy");
        if (lookupCrowdDirStrategyType == null) {
            lookupCrowdDirStrategyType = FixedCrowdDirectorySelectorFactory.class.getName();
            logger.info("Using default Directory Selector:" + lookupCrowdDirStrategyType);
        }
    }

    @Override
    protected Principal getUser(String username) {
        return getCrowdService().getUser(username);
    }

    @Override
    protected boolean authenticate(Principal principal, String s) throws AuthenticatorException {
        throw new UnsupportedOperationException("User is always authenticated by JOSSO");
    }

    @Override
    public Principal getUser(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        Principal user = httpServletRequest.getUserPrincipal();

        // Addapt SSOUser to Crowd user ...
        if (user instanceof SSOUser ) {

            SSOUser ssoUser = (SSOUser) httpServletRequest.getUserPrincipal();
            // Addapt SSO User to Crowd user

            String email = null;
            String displayName = ssoUser.getName();

            for (SSONameValuePair ssoProp : ssoUser.getProperties()) {
                if (ssoProp.getName().contains("email"))
                    email = ssoProp.getValue();
                if (ssoProp.getName().equals("displayName"))
                    displayName = ssoProp.getValue();
            }

            // Lookup proper user directory
            Directory dir = getDirSelector().lookupDirectory(ssoUser);

            ImmutableUser crowdUser = new ImmutableUser(dir.getId(), ssoUser.getName(), displayName, email, true);
            authoriseUserAndEstablishSession(httpServletRequest, httpServletResponse, crowdUser);

        } else {
            // If we don't have a valid SSO Session, return NULL!!!
            String ssoSessionId = (String) httpServletRequest.getAttribute("org.josso.agent.ssoSessionid");

            if (ssoSessionId == null || "".equals(ssoSessionId) ||  "-".equals(ssoSessionId)) {
                super.removePrincipalFromSessionContext(httpServletRequest);
                return null;
            } else {
                user = super.getUser(httpServletRequest, httpServletResponse);
            }
        }

        return user;

    }

    @Override
    public boolean login(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String s, String s1, boolean b) throws AuthenticatorException {
        throw new UnsupportedOperationException("JOSSO Agent must perform 'login' operation");
    }

    @Override
    public boolean logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticatorException {
        throw new UnsupportedOperationException("JOSSO Agent must perform 'logout' operation");
    }

    private CrowdService getCrowdService() {
        CrowdService svc = (CrowdService) ComponentManager.getComponent(CrowdService.class);
        return svc;
    }

    private CrowdDirectoryService getCrowdDirectoryService() {
        CrowdDirectoryService svc = (CrowdDirectoryService) ComponentManager.getComponent(CrowdDirectoryService.class);
        return svc;
    }

    public CrowdDirectorySelectorStrategy getDirSelector() {
        if (dirSelector == null) {
            synchronized (this) {
                if (dirSelector == null) {
                    dirSelector = CrowdDirectorySelectorFactory.getInstance(lookupCrowdDirStrategyType).getStrategy(params, getCrowdDirectoryService());
                }
            }
        }
        return dirSelector;
    }
}
