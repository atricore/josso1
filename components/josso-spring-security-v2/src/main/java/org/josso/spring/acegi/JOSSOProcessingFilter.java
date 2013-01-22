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

package org.josso.spring.acegi;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.acegisecurity.ui.AbstractProcessingFilter;
import org.acegisecurity.ui.logout.LogoutHandler;
import org.acegisecurity.ui.savedrequest.SavedRequest;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

/**
 * This is a processing filter that will inject user identity into ACEGI.  The principal is retrived from the incomming
 * HTTP request and JOSSO infrastructure is used to obtain the complete identity information.
 * <p/>
 * User: sgonzalez
 * Date: Sep 28, 2007
 * Time: 10:55:18 AM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Gianluca Brigandi</a>
 */
public class JOSSOProcessingFilter implements Filter, InitializingBean, ApplicationEventPublisherAware {

    private static final Log logger = LogFactory.getLog(JOSSOProcessingFilter.class);

    private ApplicationEventPublisher eventPublisher;

    private UserDetailsService userDetailsService;

    private LogoutHandler[] handlers;


    public void afterPropertiesSet() throws Exception {
        // Nothing to do yet.
    }


    public JOSSOProcessingFilter(LogoutHandler[] handlers) {
        this.handlers = handlers;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest)) {
            throw new IllegalArgumentException("Non HTTP request unsupported by this filter");
        }

        if (!(servletResponse instanceof HttpServletResponse)) {
            throw new IllegalArgumentException("Non HTTP response unsupported by this filter");
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // We have to provide Authentication information based on JOSSO auth information ...

        // This is the principal as injected by JOSSO in the container :
        Principal principal = request.getUserPrincipal();

        // This is the authentication information used by ACEGI
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If authentication information is present, we only need to validate that it is up to date.
        if (authentication != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Authentication information already present : '"
                        + SecurityContextHolder.getContext().getAuthentication() + "'");
            }

            // If there is no principal, we may need to logout this user ... TODO detect anonymous principals ?
            if (principal == null && authentication.isAuthenticated()) {

                // If an authenticated Authentication is present, we must issue a logout !
                if (logger.isDebugEnabled()) {
                    logger.debug("Logging out user '" + authentication + "'");
                }

                for (int i = 0; i < handlers.length; i++) {
                    handlers[i].logout(request, response, authentication);
                }

            }

            chain.doFilter(request, response);

            return;
        }

        // We have a principal but no ACEGI authentication, propagate identity from JOSSO to ACEGI.
        if (principal != null) {

            // If a saved request is present, we use the saved request to redirect the user to the original resource.
            SavedRequest savedRequest = (SavedRequest) request.getSession().getAttribute(
                    AbstractProcessingFilter.ACEGI_SAVED_REQUEST_KEY);

            if (savedRequest != null)
                logger.debug("Redirecting to original resource " + savedRequest.getFullRequestUrl());


            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            String jossoSessionId = (String) request.getAttribute("org.josso.agent.ssoSessionid");

            // New authenticated autentication instance.
            Authentication jossoAuth = new JOSSOAuthenticationToken(jossoSessionId, userDetails, userDetails.getAuthorities());

            // Store to SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(jossoAuth);
            if (logger.isDebugEnabled()) {
                logger.debug("SecurityContextHolder populated with JOSSO Authentication Token: '"
                        + SecurityContextHolder.getContext().getAuthentication() + "'");
            }

            // Fire event
            if (this.eventPublisher != null) {
                eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(
                        SecurityContextHolder.getContext().getAuthentication(), this.getClass()));
            }


            // We have a saved request, redirect to original URL ...
            if (savedRequest != null)
                response.sendRedirect(savedRequest.getFullRequestUrl());

        } else {
            if (logger.isDebugEnabled())
                logger.debug("No principal found in request !");

        }

        // Move on ...
        chain.doFilter(request, response);

    }


    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}
