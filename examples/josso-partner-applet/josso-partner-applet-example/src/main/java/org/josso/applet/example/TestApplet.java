package org.josso.applet.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.applet.agent.jaas.AppletAssertionExtractionCallbackHandler;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.applet.Applet;
import java.awt.*;
import java.security.Principal;

public class TestApplet extends Applet {

    private static final long serialVersionUID = 1L;

    private static final Log logger = LogFactory.getLog(TestApplet.class);

	private Subject subject;

	// The method will be automatically called when the applet is started
	public void init() {
		try {
            //System.setProperty("java.security.policy", this.getClass().getResource("applet.policy").toExternalForm());
            //Policy.getPolicy().refresh();

			System.setProperty("java.security.auth.login.config", this.getClass().getResource("jaas.conf").toExternalForm());
            CallbackHandler ch  = new AppletAssertionExtractionCallbackHandler(this, "");
			LoginContext lc = new LoginContext("josso", ch);
            lc.login();

			subject = lc.getSubject();

            // this.getAppletContext()
		} catch (LoginException e) {
			logger.error(e.getMessage());
		}
	}

	// This method gets called when the applet is terminated
	// (that's when the user goes to another page or exits the browser).
	public void stop() {
	}

	@Override
	public void paint(Graphics g) {
		String username = null;
        String roles = "";
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof SSOUser) {
                username = principal.getName();
            } else if (principal instanceof SSORole) {
                if (!roles.equals("")) {
                    roles += ", ";
                }
                roles += principal.getName();
            }
        }
		g.drawString("Hello " + username, 25, 25);
        g.drawString("Roles: " + roles, 25, 65);
        
        //Subject.doAs(subject, new JOSSOPrivilegedAction());
	}
}
