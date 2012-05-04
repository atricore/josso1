/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.agent.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @org.apache.xbean.XBean element="default-automaticlogin-strategy"
 *
 * @author <a href="mailto:sgonzaelz@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class DefaultAutomaticLoginStrategy extends AbstractAutomaticLoginStrategy {
    
    private static final Log log = LogFactory.getLog(DefaultAutomaticLoginStrategy.class);

    private final String NO_REFERER = "NO_REFERER";

    // List of referrers that should be ignored
    private List<String> ignoredReferrers = new ArrayList<String>();

    public DefaultAutomaticLoginStrategy() {
        super();
    }

    public DefaultAutomaticLoginStrategy(String mode) {
        super(mode);
    }

    /**
     * @org.apache.xbean.Property alias="ignoredReferrers" nestedType="java.lang.String"
     * @return
     */
    public List<String> getIgnoredReferrers() {
        return ignoredReferrers;
    }

    public void setIgnoredReferrers(List<String> ignoredReferrers) {
        this.ignoredReferrers = ignoredReferrers;
    }

    /**
     * Componenets must evaluate if automatic login is required for the received request.
     *
     * @return
     */
    @Override
    public boolean isAutomaticLoginRequired(HttpServletRequest hreq, HttpServletResponse hres) {
        
        // TODO : This should be supported by a component, for now we apply some rules on the referer:
        try {

            // TODO : This is not the best way to avoid loops when no referer is present, the flag should expire and
            // should not be attached to the SSO Session

            // The first time we access a partner application, we should attempt an automatic login.
        	Boolean autoLoginExecuted = Boolean.parseBoolean(getAgent().getAttribute(hreq, "JOSSO_AUTOMATIC_LOGIN_EXECUTED"));
            String referer = hreq.getHeader("referer");
            if (referer == null || "".equals(referer))
                referer = NO_REFERER;

            // If no referer host is found but we did not executed auto login yet, give it a try.
            if (autoLoginExecuted == null || !autoLoginExecuted) {

                if (log.isDebugEnabled())
                    log.debug("No referer found and automatic login was never executed.  Require Autologin!");

                getAgent().setAttribute(hreq, hres, "JOSSO_AUTOMATIC_LOGIN_EXECUTED", "TRUE");
                getAgent().setAttribute(hreq, hres, "JOSSO_AUTOMATIC_LOGIN_REFERER", referer);
                return true;
            }

            // If we have a referer host that differs from our we require an autologinSSs
            if (referer != null && !NO_REFERER.equals(referer)) {

                for (String ignoredReferrer : ignoredReferrers) {
                    if (referer.startsWith(ignoredReferrer)) {
                        if (log.isDebugEnabled())
                            log.debug("Referer should be ignored " + referer);
                        return false;
                    }

                }

            	String oldReferer = getAgent().getAttribute(hreq, "JOSSO_AUTOMATIC_LOGIN_REFERER");
                if (oldReferer != null && oldReferer.equals(referer)) {
                    
                    if (log.isDebugEnabled())
                        log.debug("Referer already processed " + referer);

                    // cleanup so we give this referer a chance in the future!
                    getAgent().removeAttribute(hreq, hres, "JOSSO_AUTOMATIC_LOGIN_REFERER");
                    return false;
                }

                StringBuffer mySelf = hreq.getRequestURL();
                java.net.URL myUrl = new java.net.URL(mySelf.toString());

                // This should build the base url of the java application
                String myUrlStr = myUrl.getProtocol() + "://" + myUrl.getHost() + ((myUrl.getPort() > 0 && myUrl.getPort() != 80 && myUrl.getPort() != 443) ? ":" + myUrl.getPort() : "") + hreq.getContextPath();

                if (log.isDebugEnabled())
                    log.debug("Processing referer " + referer + " for host " + myUrlStr);

                if (!referer.startsWith(myUrlStr)) {

                    if (log.isDebugEnabled())
                        log.debug("Referer found differs from current host.  Require Autologin!");

                    // Store referer for future reference!
                    getAgent().setAttribute(hreq, hres, "JOSSO_AUTOMATIC_LOGIN_REFERER", referer);
                    return true;
                }
            } else {
            	String oldReferer = getAgent().getAttribute(hreq, "JOSSO_AUTOMATIC_LOGIN_REFERER");
                if (oldReferer != null && oldReferer.equals(NO_REFERER)) {
                    if (log.isDebugEnabled())
                        log.debug("Referer already processed " + referer);
                    // Note : we are no longer removing the "referer already processed" flag since the next request
                    // it's likely that there will be no referer (browsers are no longer pushing this) and it will
                    // attempt an automatic login again .
                    //getAgent().removeAttribute(hreq, hres, "JOSSO_AUTOMATIC_LOGIN_REFERER");
                    return false;
                } else {

                    if (log.isDebugEnabled())
                        log.debug("No old Referer found.  Require Autologin!");

                	getAgent().setAttribute(hreq, hres, "JOSSO_AUTOMATIC_LOGIN_REFERER", NO_REFERER);
                	return true;
                }
            }

        } catch (MalformedURLException e) {
            this.log.debug("Error creating Referer URL : "+ e.getMessage(), e);
        } catch (Exception e) {
            this.log.debug("Cannot verify request for automatic login : " + e.getMessage(), e);
        }

        if (log.isDebugEnabled())
            log.debug("Do not Require Autologin!");

        return false;        
    }
}
