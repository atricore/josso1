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

package org.josso.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.SecurityDomain;
import org.josso.gateway.identity.exceptions.NoSuchDomainException;

import java.util.List;

/**
 *
 * @org.apache.xbean.XBean element="default-domain-selector"
 * description="JOSSO Default Security Domain Selector"
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 574 $ $Date: 2008-08-01 18:14:27 -0300 (Fri, 01 Aug 2008) $
 */
public class DomainSelectorImpl implements SSOSecurityDomainSelector {

    private static final Log logger = LogFactory.getLog(DomainSelectorImpl.class);

    /**
     * This will select the SecurityDomain associated with the received request.
     * <p/>
     * 1. If the JOSSO_SECURITY_DOMAIN_PARAM is present in the request, the value is used as security domain name.
     * <p/>
     * 2. The GWY uses the configured domain selector to find the domain.
     */
    public SecurityDomain selectDomain(SSORequest req, List<SecurityDomain> domains) throws NoSuchDomainException {

        SecurityDomain sd = matchDomain(req, domains);
        if (sd == null)
            sd = selectByName(req, domains);

        if (logger.isDebugEnabled())
            logger.debug("Selected domain is " + (sd != null ? sd.getName() : null));

        if (sd == null)
            throw new NoSuchDomainException(req);

        return sd;


    }


    /**
     * Select a domain by invoking domains matchers, ALL matchers must match to select a SecurityDomain.
     * <p/>
     * The first SecurityDomain that matches ALL matchers will be returned.
     *
     * @param req
     * @param domains
     * @return
     * @throws NoSuchDomainException
     */
    public SecurityDomain matchDomain(SSORequest req, List<SecurityDomain> domains) throws NoSuchDomainException {

        if (logger.isDebugEnabled())
            logger.debug("SecurityDomain by request: " + req);

        for (SecurityDomain sd : domains) {

            boolean match = true;
            for (SecurityDomainMatcher matcher : sd.getMatchers()) {
                if (!matcher.match(req)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                if (logger.isDebugEnabled())
                    logger.debug("Domain " + sd.getName() + " matched request " + req);
                return sd;
            }
        }

        return null;

    }

    /**
     * Selects a domain by its name.  The security domain name is looked in different scopse in the following order :
     * <br>
     * <ol>
     * <li>SSO request parameter</li>
     * <li>SSO request attribute</li>
     * </ol>
     * <p/>
     * If no name is found, this method returns null.
     *
     * @param req     the associated http request
     * @param domains the list of domains to select from
     * @return null if no name was found in request.
     * @throws NoSuchDomainException if a Security Domain Name was found in request but the domain does not exists.
     * @see org.josso.gateway.signon.Constants#KEY_JOSSO_SECURITY_DOMAIN_NAME
     */
    protected SecurityDomain selectByName(SSORequest req, List<SecurityDomain> domains) throws NoSuchDomainException {

        String name = req.getAttribute(org.josso.gateway.signon.Constants.KEY_JOSSO_SECURITY_DOMAIN_NAME);
        if (logger.isDebugEnabled())
            logger.debug("SecurityDomain by name : " + name);

        if (name == null)
            return null;

        for (SecurityDomain sd : domains) {
            if (name.equals(sd.getName())) return sd;
        }

        throw new NoSuchDomainException(name);

    }
}
