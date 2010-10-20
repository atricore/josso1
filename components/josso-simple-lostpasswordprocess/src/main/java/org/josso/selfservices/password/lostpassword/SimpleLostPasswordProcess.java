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

package org.josso.selfservices.password.lostpassword;

import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.selfservices.password.PasswordManagementProcess;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.SSOException;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Set;

/**
 * @org.apache.xbean.XBean element="lostpassword-process"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SimpleLostPasswordProcess.java 789 2008-11-27 14:58:26Z sgonzalez $
 */
public class SimpleLostPasswordProcess extends AbstractLostPasswordProcess {

    private static final Log log = LogFactory.getLog(SimpleLostPasswordProcess.class);

    private String challengeId= "email";

    private String challengeText = "Email Address";

    @Override
    public PasswordManagementProcess createNewProcess(String id) throws SSOException {
        SimpleLostPasswordProcess p = (SimpleLostPasswordProcess) super.createNewProcess(id);
        p.setChallengeId(challengeId);
        p.setChallengeText(challengeText);
        return p;
    }

    /**
     * This creates a single challenge using the value of the 'challengeId' and 'challengeText' properties.
     */
    protected ChallengeResponseCredential[] createInitilaChallenges() {

        if (log.isDebugEnabled())
            log.debug("Creating challenge ["+challengeId+"] " + challengeText);

        ChallengeResponseCredential email = new ChallengeResponseCredential(challengeId, challengeText);
        return new ChallengeResponseCredential[] {email};
    }

    /**
     * This implementation authenticates a user using the response to the initial challenge created before.
     *
     * @throws AuthenticationFailureException if no challenge is received with the configured challengeId or a user
     *  cannot be authenticated whit the challenge response.
     */
    protected SSOUser authenticate(Set<ChallengeResponseCredential> challenges) throws AuthenticationFailureException {

        ChallengeResponseCredential challenge = getChallenge(challengeId, challenges);
        if (challenge == null)
            throw new AuthenticationFailureException("No challenge received : " + challengeId);

        try {

            String username = getIdentityManager().findUsernameByRelayCredential(challenge);
            if (username == null)
                throw new AuthenticationFailureException("No user found for provided challenges");

            SSOUser user = findUserByUsername(username);
            log.debug("User found for " + username);

            return user;

        } catch (SSOIdentityException e) {
            log.error(e.getMessage(), e);
            throw new AuthenticationFailureException("No email received");
        }

    }

    // ------------------------------------------------------------------------------

    /**
     * @org.apache.xbean.Property alias="challeng-id"
     *
     * @return
     */
    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    /**
     * @org.apache.xbean.Property alias="challenge-text"
     * @return
     */
    public String getChallengeText() {
        return challengeText;
    }

    public void setChallengeText(String challengeText) {
        this.challengeText = challengeText;
    }
}
