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
package org.josso.gateway.protocol.handler;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.smb.NtlmChallenge;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;
import jcifs.util.LogStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.Lookup;
import org.josso.auth.BaseCredential;
import org.josso.auth.Credential;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.auth.scheme.NtlmPasswordAuthenticationCredential;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @org.apache.xbean.XBean element="ntlm-protocol-handler"
 *
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 25, 2008
 * Time: 2:21:53 PM
 * To change this template use File | Settings | File Templates.
 *
 * @org.apache.xbean.XBean element="protocol-handler"
 */
public class NtlmProtocolHandler implements ProtocolHandler, InitializingBean {
    private static final Log logger = LogFactory.getLog(NtlmProtocolHandler.class);
    private static LogStream jcifsLog = LogStream.getInstance();

    public static final String NTLM_DOMAIN_CONTROLLER = "ntlmHttpDc";

    public static final String NTLM_PASS_AUTHENTICATION = "ntlmHttpPa";

    public static final String NTLM_ERROR_FLAG = "ntlm_error";

    public static final String NTLM_ERROR_COUNT = "ntlm_error_count";

    // ----------------------------------------------------- NTLM Fields
    private String defaultDomain;
    private String wins;
    private String domainController;
    private boolean loadBalance;
    private boolean enableBasic;
    private boolean insecureBasic;
    private String realm;
    private String preAuthUsername;
    private String preAuthPassword;
    private boolean log;

    public String getDefaultDomain() {
        return defaultDomain;
    }

    public void setDefaultDomain(String defaultDomain) {
        this.defaultDomain = defaultDomain;
    }

    public String getWins() {
        return wins;
    }

    public void setWins(String wins) {
        this.wins = wins;
    }

    public String getDomainController() {
        return domainController;
    }

    public void setDomainController(String domainController) {
        this.domainController = domainController;
    }

    public boolean getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.setLoadBalance(Boolean.getBoolean(loadBalance));
    }

    public void setLoadBalance(boolean loadBalance) {
        this.loadBalance = loadBalance;
    }

    public boolean getEnableBasic() {
        return enableBasic;
    }

    public void setEnableBasic(String enableBasic) {
        this.setEnableBasic(Boolean.getBoolean(enableBasic));
    }

    public void setEnableBasic(boolean enableBasic) {
        this.enableBasic = enableBasic;
    }

    public boolean getInsecureBasic() {
        return insecureBasic;
    }

    public void setInsecureBasic(String insecureBasic) {
        this.setInsecureBasic(Boolean.getBoolean(insecureBasic));
    }

    public void setInsecureBasic(boolean insecureBasic) {
        this.insecureBasic = insecureBasic;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setPreAuthUsername(String preAuthUsername) {
        this.preAuthUsername = preAuthUsername;
    }

    public String getPreAuthPassword() {
        return preAuthPassword;
    }

    public void setPreAuthPassword(String preAuthPassword) {
        this.preAuthPassword = preAuthPassword;
    }

    public boolean getLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    private boolean isOfferBasic(HttpServletRequest req) {
        return enableBasic && (insecureBasic || req.isSecure());
    }


    // ----------------------------------------------------- Spring lifecycle handlers
    public void afterPropertiesSet() throws Exception {


        if (preAuthUsername == null )
                throw new IllegalArgumentException("preAuthUsername attribute must be declared");
        Config.setProperty("jcifs.smb.client.username", preAuthUsername);

        if (preAuthPassword == null )
                throw new IllegalArgumentException("preAuthPassword attribute must be declared");
        Config.setProperty("jcifs.smb.client.password", preAuthPassword);
        
        /* Set jcifs properties we know we want; soTimeout and cachePolicy to 30min.
         */
        Config.setProperty( "jcifs.smb.client.soTimeout", "1800000" );
        Config.setProperty( "jcifs.netbios.cachePolicy", "1200" );
        /* The protocol handler can only work with NTLMv1 as it uses a man-in-the-middle
         * techinque that NTLMv2 specifically thwarts. A real NTLM Filter would
         * need to do a NETLOGON RPC that JCIFS will likely never implement
         * because it requires a lot of extra crypto not used by CIFS.
         */
        Config.setProperty( "jcifs.smb.client.useExtendedSecurity", "false" );
        Config.setProperty( "jcifs.smb.lmCompatibility", "0");


        if (getWins() != null)
            Config.setProperty( "jcifs.netbios.wins", getWins()); 

        LogStream.setLevel( log ? 10 : -1);
        Config.setProperty( "jcifs.util.loglevel", log ? "10" : "-1");

        if ( log ) {
            try {
                Config.store( jcifsLog, "JCIFS PROPERTIES" );
            } catch( IOException ioe ) {
            }
        }        
    }

    // ----------------------------------------------------- Constructors
    public NtlmProtocolHandler() {

    }

    // ----------------------------------------------------- ProtocolHandler methods implementations
    public boolean acceptJob(HttpServletRequest request, HttpServletResponse response) {
        // TODO: discriminate if it's really time to engage in an ntlm negotiaion (e.g. http request introspection)
        String err = (String) request.getSession().getAttribute(NTLM_ERROR_FLAG);
        if (err != null) {

            if (err.equals("AUTHN_ERROR")) {
                Integer errCount = (Integer) request.getSession().getAttribute(NTLM_ERROR_COUNT);
                if (errCount < 2)
                    return true;
            }
            
            request.setAttribute(NTLM_ERROR_FLAG, err);
            return false;
        }

        return true;
    }

    public boolean doJob(HttpServletRequest request, HttpServletResponse response) {


        try {
            return negotiate(request, response, false);
        } catch (Exception e) {
            // Mark this as fatal error ...
            request.setAttribute(NTLM_ERROR_FLAG, "FATAL_ERROR");
            logger.error("Error during NTLM handshake : " + e.getMessage(), e);
        } finally {
            String err = (String) request.getAttribute(NTLM_ERROR_FLAG);
            if (err != null) {
                request.getSession().setAttribute(NTLM_ERROR_FLAG, err);

                if (err.equals("AUTHN_ERROR")) {
                    Integer errCount = 0;
                    if (request.getSession().getAttribute(NTLM_ERROR_COUNT) != null)
                        errCount = (Integer) request.getSession().getAttribute(NTLM_ERROR_COUNT);

                    errCount ++;
                    request.getSession().setAttribute(NTLM_ERROR_COUNT, errCount);

                } else {
                    request.getSession().setAttribute(NTLM_ERROR_COUNT, null);
                }

            }
        }

        // Let the request be processed later
        return true;
    }

    public boolean authenticate(Credential[] credentials) throws AuthenticationFailureException {
        try {
            return this.authenticateCredentials(credentials);
        } catch (SmbException se) {
            throw new AuthenticationFailureException(se.getMessage(), Integer.toHexString(se.getNtStatus()));
        }
    }

    // ----------------------------------------------------- NtlmProtocolHandler methods
    protected boolean negotiate(HttpServletRequest req,
                                HttpServletResponse resp,
                                boolean skipAuthentication) throws IOException, ServletException {
        UniAddress dc;
        String msg;
        NtlmPasswordAuthentication ntlm;
        msg = req.getHeader("Authorization");

        // Checks for loging errors
        Object error = req.getAttribute(NTLM_ERROR_FLAG);
        if (error != null) {
            if (logger.isDebugEnabled())
                logger.debug("Restarts negotiation due to authentication error");

            req.removeAttribute(NTLM_ERROR_FLAG);
            this.startsNegotiation(req, resp);
            return false;
        }

        //if no error go on with the default handshake
        if (msg != null && (msg.startsWith("NTLM ") || (this.isOfferBasic(req) && msg.startsWith("Basic ")))) {
            if (msg.startsWith("NTLM ")) {
                HttpSession ssn = req.getSession();
                byte[] challenge;

                if (loadBalance) {
                    NtlmChallenge chal = (NtlmChallenge) ssn.getAttribute("NtlmHttpChal");
                    if (chal == null) {
                        logger.debug("SMB:getChallengeForDomain START");
                        chal = SmbSession.getChallengeForDomain();
                        logger.debug("SMB:getChallengeForDomain END");
                        ssn.setAttribute("NtlmHttpChal", chal);
                    }
                    dc = chal.dc;
                    challenge = chal.challenge;
                } else {
                    dc = UniAddress.getByName(domainController, true);
                    logger.debug("SMB:getChallenge " + dc.getHostAddress() + " START");
                    challenge = SmbSession.getChallenge(dc);
                    logger.debug("SMB:getChallenge END");
                }

                // NTLM Authentication
                logger.debug("NtlmSsp:authenticate START");
                if ((ntlm = NtlmSsp.authenticate(req, resp, challenge)) == null) {
                    // Auth Failed
                    req.setAttribute(NTLM_ERROR_FLAG, "AUTHN_ERROR");
                    logger.debug("NtlmSsp:authenticate END (ERR)");
                    return true;
                }
                logger.debug("NtlmSsp:authenticate END");

                /* negotiation complete, remove the challenge object */
                ssn.removeAttribute("NtlmHttpChal");
            } else {
                String auth = new String(Base64.decode(msg.substring(6)), "US-ASCII");
                int index = auth.indexOf(':');
                String user = (index != -1) ? auth.substring(0, index) : auth;
                String password = (index != -1) ? auth.substring(index + 1) : "";
                index = user.indexOf('\\');
                if (index == -1) index = user.indexOf('/');
                String domain = (index != -1) ? user.substring(0, index) : defaultDomain;
                user = (index != -1) ? user.substring(index + 1) : user;
                ntlm = new NtlmPasswordAuthentication(domain, user, password);
                dc = UniAddress.getByName(domainController, true);
            }

            req.getSession().setAttribute(NTLM_DOMAIN_CONTROLLER, dc);
            req.getSession().setAttribute(NTLM_PASS_AUTHENTICATION, ntlm);

        } else {
            if (!skipAuthentication) {
                HttpSession ssn = req.getSession(false);
                if (ssn == null || (ssn.getAttribute(NTLM_PASS_AUTHENTICATION) == null)) {
                    this.startsNegotiation(req, resp);
                    return false;
                }
            }
        }

        return true;
    }

    private void startsNegotiation(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "NTLM");
        if (this.isOfferBasic(request))
            response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentLength(0);
        response.flushBuffer();
    }

    private boolean authenticateCredentials(Credential[] credentials) throws SmbException {
        if (credentials.length != 2) {
            logger.error("Spected 2 credencials, received " + credentials.length);
            return false;
        }

        Object o1 = ((BaseCredential) credentials[0]).getValue();
        Object o2 = ((BaseCredential) credentials[1]).getValue();

        if (o1 == null || o2 == null) {
            logger.error("Some or all of the credential values are null");
            return false;
        }

        if (o1 instanceof UniAddress && o2 instanceof NtlmPasswordAuthentication) {
            return this.authenticate((UniAddress) o1, (NtlmPasswordAuthentication) o2);
        } else if (o2 instanceof UniAddress && o1 instanceof NtlmPasswordAuthentication) {
            return this.authenticate((UniAddress) o2, (NtlmPasswordAuthentication) o1);
        } else {
            logger.error("The credential types could not be managed");
            logger.error("  Credential 1 is " + o1);
            logger.error("  Credential 2 is " + o2);
        }

        return false;

    }

    private boolean authenticate(UniAddress dc, NtlmPasswordAuthentication ntlm) throws SmbException {
        logger.debug("SMB:logon START!");
        SmbSession.logon(dc, ntlm);
        logger.debug("SMB:logon END!");

        if (logger.isDebugEnabled()) {
            logger.debug("[authenticate()]" + ntlm + " successfully authenticated against " + dc);
        }
        return true;
    }

    // ----------------------------------------------------- Credential method handlers
    public static String getPasswordAuthentication(NtlmPasswordAuthenticationCredential credential) {
        NtlmPasswordAuthentication pa = (NtlmPasswordAuthentication) credential.getValue();
        return pa == null ? "" : pa.getUsername();
    }


    @Override
    public String toString () {
        return "{ [Default Domain=" + defaultDomain +
                "] [Domain Controller=" + domainController +
                "] [Wins=" + wins +
                "] [Load Balance=" + loadBalance +
                "] [Enable Basic=" + enableBasic +
                "] [Insecure Basic=" + insecureBasic +
                "] [Realm=" + realm +
                "] [Preauthentication Username=" + preAuthUsername +
                "] [Preauthentication Password=" + preAuthPassword +
                "] [Log=" + log +
                "] }";
    }

    public String getPreAuthUsername() {
        return preAuthUsername;
    }

}