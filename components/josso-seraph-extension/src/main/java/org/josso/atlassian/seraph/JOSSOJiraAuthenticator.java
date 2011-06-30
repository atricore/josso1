package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfig;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Map;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public class JOSSOJiraAuthenticator extends DefaultAuthenticator {

    private int directoryId = 10000;

    public int getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(int directoryId) {
        this.directoryId = directoryId;
    }

    @Override
    public void init(Map<String, String> params, SecurityConfig config) {
        super.init(params, config);
        String strDirId = params.get("directory.id");
        if (strDirId != null)
            directoryId = Integer.parseInt(strDirId);
    }

    @Override
    protected Principal getUser(String username) {
        return OSUserConverter.convertToOSUser(getCrowdService().getUser(username));
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

            ///

            ImmutableUser crowdUser = new ImmutableUser(getDirectoryId(), ssoUser.getName(), displayName, email, true);

            user = OSUserConverter.convertToOSUser(crowdUser);

            authoriseUserAndEstablishSession(httpServletRequest, httpServletResponse, user);

        } else {
            user = super.getUser(httpServletRequest, httpServletResponse);
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
        return (CrowdService) ComponentManager.getComponent(CrowdService.class);
    }


}
