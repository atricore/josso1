package org.josso.agent.http;

import org.josso.agent.SSOPartnerAppConfig;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.signon.Constants;

import javax.servlet.http.HttpServletRequest;

/**
 * @org.apache.xbean.XBean element="appid-parameters-builder"
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class AppIdParametersBuilder extends AbstractFrontChannelParametersBuilder {

    @Override
    public SSONameValuePair[] buildParamters(SSOPartnerAppConfig cfg, HttpServletRequest hreq) {
        return new SSONameValuePair[] { new SSONameValuePair(Constants.PARAM_JOSSO_PARTNERAPP_ID, cfg.getId())} ; 
    }
}
