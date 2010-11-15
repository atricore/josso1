package org.josso.applet.agent.jaas;

import netscape.javascript.JSObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.Constants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.applet.Applet;
import java.io.IOException;
import java.util.Properties;

public class AppletAssertionExtractionCallbackHandler implements CallbackHandler {

    private static final Log logger = LogFactory.getLog(AppletAssertionExtractionCallbackHandler.class);

	private Applet applet;
    private String requester;

    public AppletAssertionExtractionCallbackHandler(Applet applet, String requester) {
        this.applet = applet;
        this.requester = requester;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback) callbacks[i];

                if (nc.getPrompt().equals("appID"))
                    nc.setName(requester);
                else if (nc.getPrompt().equals("endpoint"))
                    nc.setName(getEndpoint());
                else
                    nc.setName(getCookie(Constants.JOSSO_SINGLE_SIGN_ON_COOKIE));

            } else {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }

    protected String getCookie(String name) {
		JSObject myBrowser = JSObject.getWindow(applet);
        JSObject myDocument =  (JSObject) myBrowser.getMember("document");
        String myCookie = (String) myDocument.getMember("cookie");
		String search = name + "=";
		if (myCookie.length() > 0) {
			int offset = myCookie.indexOf(search);
			if (offset != -1) {
				offset += search.length();
				int end = myCookie.indexOf(";", offset);
				if (end == -1) end = myCookie.length();
				return myCookie.substring(offset,end);
			} else {
				return null;
			}
		}
		return null;
	}

    protected String getEndpoint() {
		String endpoint = null;
        Properties agentProperties = new Properties();
        try {
            agentProperties.load(this.getClass().getResource("/META-INF/josso/agent.properties").openStream());
            endpoint = agentProperties.getProperty("ws.locator.endpoint");
        } catch (IOException e) {
            logger.error("WS endpoint isn't configured.", e);
        }
        return endpoint;
	}
}
