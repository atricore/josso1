package org.josso.agent.http;

import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SSOPartnerAppConfig;
import org.josso.gateway.SSONameValuePair;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public abstract class AbstractFrontChannelParametersBuilder implements FrontChannelParametersBuilder {

    public SSONameValuePair[] buildParamters(SSOPartnerAppConfig cfg, HttpServletRequest hreq) {
        return new SSONameValuePair[0];
    }
}
