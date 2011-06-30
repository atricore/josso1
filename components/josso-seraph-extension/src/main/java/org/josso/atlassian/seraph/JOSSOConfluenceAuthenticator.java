package org.josso.atlassian.seraph;

import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfig;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;
import com.atlassian.event.EventManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.UserManager;
import com.atlassian.spring.container.ContainerManager;

import com.atlassian.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Map;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public class JOSSOConfluenceAuthenticator extends DefaultAuthenticator {

    private EventManager eventManager;

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
        try {
            return getUserManager().getUser(username);
        } catch (EntityException e) {
        }
        return null;
    }

    @Override
    protected boolean authenticate(Principal principal, String s) throws AuthenticatorException {
        throw new UnsupportedOperationException("User is always authenticated by JOSSO");
    }

    @Override
    public Principal getUser(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        Principal user = httpServletRequest.getUserPrincipal();

        // Addapt SSOUser to Crowd user ...
        if (user instanceof SSOUser) {

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

            ImmutableUser crowdUser = new ImmutableUser(getDirectoryId(), ssoUser.getName(), displayName, email, true);

            String remoteIP = httpServletRequest.getRemoteAddr();
            String remoteHost = httpServletRequest.getRemoteHost();

            getEventManager().publishEvent(new LoginEvent(this, ssoUser.getName(), httpServletRequest.getSession().getId(), remoteHost, remoteIP));

            user = crowdUser;

        } else {
            user = super.getUser(httpServletRequest, httpServletResponse);
        }

        return user;

    }

    protected UserManager getUserManager() {
        return (UserManager) ContainerManager.getComponent("userManager");
    }

    protected EventManager getEventManager() {
        if (this.eventManager == null) {
            this.eventManager = ((EventManager) ContainerManager.getInstance().getContainerContext().getComponent("eventManager"));
        }
        return this.eventManager;
    }


}
