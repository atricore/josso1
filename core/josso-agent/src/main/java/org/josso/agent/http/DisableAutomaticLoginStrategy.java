package org.josso.agent.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @org.apache.xbean.XBean element="disabled-automaticlogin-strategy"
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class DisableAutomaticLoginStrategy extends AbstractAutomaticLoginStrategy {

    private static final Log log = LogFactory.getLog(DisableAutomaticLoginStrategy.class);

    @Override
    public boolean isAutomaticLoginRequired(HttpServletRequest hreq, HttpServletResponse hres) {
        return false;
    }
}
