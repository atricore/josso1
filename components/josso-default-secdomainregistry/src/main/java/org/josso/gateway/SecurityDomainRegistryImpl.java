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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @org.apache.xbean.XBean element="default-domains-registry" 
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev$ $Date$
 */
public class SecurityDomainRegistryImpl implements SecurityDomainRegistry {

    private static final Log logger = LogFactory.getLog(SecurityDomainRegistryImpl.class);

    private Map<String, SecurityDomainRecord> domainRecords = new HashMap<String, SecurityDomainRecord>();

    public SecurityDomain lookup(String tokenType, String token) {
        for (SecurityDomainRecord r : domainRecords.values()) {
            if (r.hasToken(tokenType, token))
                return r.getSecurityDomain();
        }

        return null;
    }

    public synchronized void register(SecurityDomain sd) {
        logger.info("Registering SecurityDomain : " + sd.getName());
        domainRecords.put(sd.getName(), new SecurityDomainRecord(sd));
    }

    public synchronized void unregister(String securityDomainName) {
        logger.info("Unregistering SecurityDomain : " + securityDomainName);
        domainRecords.remove(securityDomainName);
    }


    public synchronized void registerToken(String securityDomainName, String tokenType, String token) {
        if (securityDomainName == null)
            throw new IllegalArgumentException("Security Domain Name cannot be null");

        if (logger.isDebugEnabled())
            logger.debug("Registering security token " + securityDomainName + " [" + tokenType + "/" + token + "]");

        SecurityDomainRecord r = domainRecords.get(securityDomainName);
        r.addToken(tokenType, token);
    }

    public synchronized void unregisterToken(String securityDomainName, String tokenType, String token) {
        if (securityDomainName == null)
            throw new IllegalArgumentException("Security Domain Name cannot be null");

        if (logger.isDebugEnabled())
            logger.debug("Unregistering security token " + securityDomainName + " [" + tokenType + "/" + token + "]");

        SecurityDomainRecord r = domainRecords.get(securityDomainName);

        if (r != null)
            r.removeToken(tokenType, token);
    }

    protected class SecurityDomainRecord {

        private SecurityDomain sd;
        private Map<String, Set<String>> tokens = new HashMap<String, Set<String>>();

        public SecurityDomainRecord(SecurityDomain sd) {
            this.sd = sd;
        }

        public SecurityDomain getSecurityDomain() {
            return sd;
        }

        public boolean hasToken(String type, String token) {
            Set<String> tokenValues = tokens.get(type);
            return tokenValues != null && tokenValues.contains(token);
        }

        public void addToken(String type, String token) {
            Set<String> tokenValues = tokens.get(type);
            if (tokenValues == null) {
                tokenValues = new HashSet<String>();
                tokens.put(type, tokenValues);
            }

            tokenValues.add(token);

        }

        public void removeToken(String type, String token) {
            Set<String> tokenValues = tokens.get(type);
            if (tokenValues == null)
                return;

            tokenValues.remove(token);
        }

    }
}
