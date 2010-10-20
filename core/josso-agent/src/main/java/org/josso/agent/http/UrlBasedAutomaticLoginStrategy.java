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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This strategy returns false if the accessed URL matches any of the configured URL patterns.
 * The patterns are regular expressions.
 *
 * @org.apache.xbean.XBean element="urlbased-automaticlogin-strategy"
 * 
 * @author <a href="mailto:sgonzaelz@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class UrlBasedAutomaticLoginStrategy extends AbstractAutomaticLoginStrategy {

	private static final Log log = LogFactory.getLog(UrlBasedAutomaticLoginStrategy.class);

	private List<String> urlPatterns;
	
    public UrlBasedAutomaticLoginStrategy() {
        super();
    }

    public UrlBasedAutomaticLoginStrategy(String mode) {
        super(mode);
    }
    
    /**
     * Components must evaluate if automatic login is required for the received request.
     *
     * @return
     */
    @Override
    public boolean isAutomaticLoginRequired(HttpServletRequest hreq, HttpServletResponse hres) {
    	boolean autoLoginRequired = true;
    	if (urlPatterns != null && urlPatterns.size() > 0) {
    		String requestURL = getRequestURL(hreq);
    		for (String urlPattern : urlPatterns) {
    			Pattern p = Pattern.compile(urlPattern);
    			Matcher m = p.matcher(requestURL);
    			if (m.matches()) {
    				if (log.isDebugEnabled())
                        log.debug("Autologin is not required! Ignored url pattern: " + urlPattern);
    				autoLoginRequired = false;
    				break;
    			}
    		}
    	}
    	return autoLoginRequired;
    }

    /**
     * Gets request url from the given request.
     * 
     * @param hreq http request
     * @return request url
     */
    private String getRequestURL(HttpServletRequest hreq) {
    	StringBuffer sb = new StringBuffer(hreq.getRequestURI());
        if (hreq.getQueryString() != null) {
            String q = hreq.getQueryString();
            if (!q.startsWith("?"))
                sb.append('?');
            sb.append(q);
        }
        return sb.toString();
    }
    
	/**
	 * @return the urlPatterns
	 */
	public List<String> getUrlPatterns() {
		return urlPatterns;
	}

	/**
	 * @param urlPatterns the urlPatterns to set
	 */
	public void setIgnoredUrlPatterns(List<String> urlPatterns) {
		this.urlPatterns = urlPatterns;
	}
}
