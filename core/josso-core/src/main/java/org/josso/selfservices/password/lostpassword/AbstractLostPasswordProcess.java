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

import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.service.SSOIdentityManager;
import org.josso.gateway.SSOException;
import org.josso.selfservices.password.*;
import org.josso.selfservices.annotations.Action;
import org.josso.selfservices.annotations.Extension;
import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.selfservices.ProcessRequest;
import org.josso.selfservices.ProcessResponse;
import org.josso.selfservices.ProcessState;
import org.josso.auth.Credential;
import org.josso.auth.CredentialProvider;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.util.id.IdGenerator;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Set;
import java.util.HashSet;

/**
 * Base service class with standard utils.
 *
 * Next JOSSO Architecture will use a BPM engine for this kind of stuf
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: AbstractLostPasswordProcess.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public abstract class AbstractLostPasswordProcess extends BasePasswordManagementProcess implements Constants {

    private static final Log log = LogFactory.getLog(AbstractLostPasswordProcess.class);

    private CredentialProvider credentialProvider;

    private PasswordDistributor distributor;

    private PasswordGenerator generator;

    private SSOIdentityManager identityManager;

    private IdGenerator idGenerator;

    @Extension( EXT_URL_PROVIDER)
    private LostPasswordUrlProvider urlProvider;

    // ---------------------------------------------------------------------------------------------------

    @Override
    public PasswordManagementProcess createNewProcess(String id) throws SSOException {
        AbstractLostPasswordProcess newProcess = (AbstractLostPasswordProcess) super.createNewProcess(id);

        // newProcess.setUserStore(extendedIdentityStore);
        newProcess.setCredentialProvider(credentialProvider);
        newProcess.setIdentityManager(identityManager);
        newProcess.setPasswordDistributor(distributor);
        newProcess.setPasswordGenerator(generator);
        newProcess.setIdGenerator(idGenerator);

        return newProcess;
    }

    @Override
    protected ProcessState doMakeState(String id) {
        return new LostPasswordProcessState(id);
    }

    // -----------------------------------------------------------< Process actions >

    @Override
    public ProcessResponse start() {

        if (log.isDebugEnabled())
            log.debug("Starting lost password process .. .");
        super.start();

        assert this.identityManager != null : "No Identity Manager Configured";
        assert this.credentialProvider != null : "No Credential Provider Configured";
        assert this.distributor != null : "No Password Distributor Configured";
        assert this.generator != null : "No Password Generator Configured";
        assert this.idGenerator != null : "No Password Assertion Generator Configured";

        // Clear state
        getLostPasswordState().setPasswordConfirmUrl(null);
        getLostPasswordState().setAssertionId(null);
        getLostPasswordState().setChallenges(new HashSet<ChallengeResponseCredential>());

        // Start
        ChallengeResponseCredential[] challenges = createInitilaChallenges();
        // In this case, credentials are required!
        ProcessResponse response = createResponse(STEP_REQUEST_CHALLENGES);
        response.setAttribute(ATTR_CHALLENGES, challenges);
        storeAllChallenges(challenges);

        return response;

    }

    @Override
    public void stop() {

        if (log.isDebugEnabled())
            log.debug("Stopping lost password process .. .");

        super.stop();
    }

    @Action( fromSteps = {STEP_REQUEST_CHALLENGES, STEP_REQUEST_ADDITIONAL_CHALLENGES} )
    public ProcessResponse processChallenges(ProcessRequest request) {

        try {

            // Store already received challenges
            ChallengeResponseCredential[] c = (ChallengeResponseCredential[]) request.getAttribute(ATTR_CHALLENGES);
            if (c == null) {

                if (log.isDebugEnabled())
                    log.debug("No challenges received!");

                return createFinalResponse(STEP_AUTH_ERROR);
            }

            storeAllChallenges(c);
            Set<ChallengeResponseCredential> challenges = retrieveAllChallenges();

            ChallengeResponseCredential[] additionalChallenges = createAdditionalChallenges(challenges);
            if (additionalChallenges != null && additionalChallenges.length > 0 ) {

                if (log.isDebugEnabled())
                    log.debug("Requesting additional challengis");

                ProcessResponse response = createResponse(STEP_REQUEST_ADDITIONAL_CHALLENGES);
                response.setAttribute(ATTR_CHALLENGES, additionalChallenges);
                storeAllChallenges(additionalChallenges);
                return response;
            }

            if (log.isDebugEnabled())
                log.debug("Starting password reset");

            // Authenticate user !
            SSOUser user = authenticate (challenges);
            if (log.isDebugEnabled())
                log.debug("User " + user.getName() + " authenticated");

            String clearPassword = createNewPassword(user, challenges);
            if (log.isDebugEnabled())
                log.debug("Password created for " + user.getName());

            Credential password = credentialProvider.newEncodedCredential("password", clearPassword);
            if (log.isDebugEnabled())
                log.debug("Password encoded for " + user.getName());

            this.getLostPasswordState().setUser(user);
            this.getLostPasswordState().setNewPasswordCredential(password);

            String passwordAssertionId = generateAssertionId(user);

            if (log.isDebugEnabled())
                log.debug("Password Assertion ID [" + passwordAssertionId + "] generated for " + user.getName());

            getLostPasswordState().setAssertionId(passwordAssertionId);
            getLostPasswordState().setPasswordConfirmUrl(urlProvider.provideResetUrl(passwordAssertionId));

            distribute(user, clearPassword);
            if (log.isDebugEnabled())
                log.debug("Password distributed " + user.getName());

            // Request confirmation challenges after password distribution
            ProcessResponse response = createResponse(STEP_CONFIRM_PASSWORD);
            ChallengeResponseCredential[] confirmationChallenges = createConfirmationChallenges();
            if (confirmationChallenges != null && confirmationChallenges.length > 0 ) {
                storeAllChallenges(confirmationChallenges);
                response.setAttribute(ATTR_CHALLENGES, confirmationChallenges );
            }

            return response;

        } catch (AuthenticationFailureException e) {
            log.error("Authentication error " + e.getMessage(), e);
            ProcessResponse response = createFinalResponse(STEP_AUTH_ERROR);
            response.setAttribute("error", e);
            return response;

        } catch (Exception  e) {
            log.error("Fatal error error " + e.getMessage(), e);
            ProcessResponse response = createFinalResponse(STEP_FATAL_ERROR);
            response.setAttribute("error", e);
            return response;

        }

    }

    @Action (fromSteps = {STEP_CONFIRM_PASSWORD, STEP_REQUEST_ADDITIONAL_CONFIRMATION_CHALLENGES})
    public ProcessResponse requestPasswordConfirmation(ProcessRequest request)  throws PasswordManagementException {

        ChallengeResponseCredential[] c = (ChallengeResponseCredential[]) request.getAttribute(ATTR_CHALLENGES);
        storeAllChallenges(c);

        ChallengeResponseCredential[] additionalChallenges = this.createAdditionalConfirmationChallenges(retrieveAllChallenges());
        if (additionalChallenges != null && additionalChallenges.length > 0) {
            ProcessResponse response = createFinalResponse(STEP_REQUEST_ADDITIONAL_CONFIRMATION_CHALLENGES);
            response.setAttribute(ATTR_CHALLENGES, additionalChallenges);
            storeAllChallenges(additionalChallenges);
            return response;
        }

        Set<ChallengeResponseCredential> challenges = retrieveAllChallenges();
        if (challenges != null && challenges.size() > 0) {
            try {
                authenticateConfirmation();

                SSOUser user = this.getLostPasswordState().getUser();

                if (log.isDebugEnabled())
                    log.debug("Password confirmed for " + user.getName());

                Credential password = this.getLostPasswordState().getNewPasswordCredential();
                updateAccount(user, password);
                if (log.isDebugEnabled())
                    log.debug("Account updated : " + user.getName());


            } catch (AuthenticationFailureException e) {
                log.error(e.getMessage(), e);
                return createFinalResponse(STEP_AUTH_ERROR);
            }

        } else {
            // No challenges provided or requested!
            log.error("No challenges provided or requested for password confirmation!");
            return createFinalResponse(STEP_AUTH_ERROR);
        }

        return createFinalResponse(STEP_PASSWORD_RESETED);
    }

    @Action ( fromSteps = { STEP_PASSWORD_RESETED} )
    public ProcessResponse passwordResetted(ProcessRequest request) {
        this.stop();
        return createFinalResponse(null);
    }

    @Action ( fromSteps = { STEP_FATAL_ERROR})
    public ProcessResponse fatalError(ProcessRequest r) {
        this.stop();
        return createFinalResponse(null);
    }

    @Action ( fromSteps = { STEP_AUTH_ERROR})
    public ProcessResponse authError(ProcessRequest r) {
        this.stop();
        return createFinalResponse(null);
    }


    // ----------------------------------------------------------< Process Primitive methods >

    // Subclasses should override this.

    /**
     * This are the challenges presented to the user when pasword recovery is started.
     */
    protected ChallengeResponseCredential[] createInitilaChallenges() {
        return null;
    }

    /**
     * This are additional challenges, created based on the initial challenges, this can be for example, specific
     * secrect questions selected by the user on registration.  If no additional challenges are required this method
     * must return null.
     *
     * Before creating any challenge, make sure that the challeng was not previously created.
     *
     * @see #getChallenge(String)
     *
     */
    protected ChallengeResponseCredential[] createAdditionalChallenges(Set<ChallengeResponseCredential> challenges) {
        return null;
    }

    /**
     * This are challenges presented when the password change is confirmed. If no challenges are required on confirmation
     * this method must return null. This implementation requests the password assertion challenge.
     *
     * Before creating any challenge, make sure that the challeng was not previously created.
     *
     * @see #getChallenge(String)
     */
    protected ChallengeResponseCredential[] createConfirmationChallenges() {

        if (getChallenge(CHALLENGE_PWD_ASSERTION_ID) != null) {
            if (log.isDebugEnabled())
                log.debug("Already created password assertion challenge, value is " + getChallenge(CHALLENGE_PWD_ASSERTION_ID).getValue());
            return null;
        }

        log.debug("Creating password assertion challenge");

        ChallengeResponseCredential c = new ChallengeResponseCredential (CHALLENGE_PWD_ASSERTION_ID, "Password Assertion");
        return new ChallengeResponseCredential [] {c};
    }

    /**
     * This are additional challenges, created based on the initial challenges, this can be for example, specific
     * secrect questions selected by the user on registration.  If no additional challenges are required this method
     * must return null.
     *
     * Before creating any challenge, make sure that the challeng was not previously created.
     *
     * @see #getChallenge(String)
     *
     */
    protected ChallengeResponseCredential[] createAdditionalConfirmationChallenges(Set<ChallengeResponseCredential> challenges) {
        return null;
    }



    /**
     * Authenticates a user based on a set of challenges.  This method will be invoked after initial and additional
     * challenges are collected.  All challenges are sent.
     */
    protected abstract SSOUser authenticate(Set<ChallengeResponseCredential> challenges) throws AuthenticationFailureException;

    /**
     * This can authenticat confirmation credentials.  This is invoked if confirmation credentials were requested.
     */
    protected void authenticateConfirmation() throws AuthenticationFailureException {

        ChallengeResponseCredential c = getChallenge(CHALLENGE_PWD_ASSERTION_ID);
        if (c == null || c.getValue() == null)
            throw new AuthenticationFailureException("No Password Assertion found");

        String assertionId = (String) c.getValue();
        if (!assertionId.equals(getPasswordAssertionId())) {
            log.error("Invalid password assertion  : " + assertionId);
            throw new AuthenticationFailureException("Invalid password assertion : " + getPasswordAssertionId());
        }

        // all ok !
    }

    /**
     * This method actually performs password reset.
     */
    protected String createNewPassword(SSOUser user, Set<ChallengeResponseCredential> challenges) {
        if (log.isDebugEnabled())
            log.debug("Generating new password for " + user.getName());

        return this.generator.generateClearPassword(user, retrieveAllChallenges());
    }

    /**
     * This updates the user account with the new password.  implementationc can also perform additional tasks like
     * unlocking the user account.
     *
     * @param user
     * @param password
     */
    protected void updateAccount(SSOUser user, Credential password) throws PasswordManagementException {
        if (log.isDebugEnabled())
            log.debug("Updating user account for " + user.getName());

        try {
           identityManager.updateAccountPassword(user, password);
        } catch (SSOIdentityException e) {
            throw new PasswordManagementException(e.getMessage(), e);
        }
    }

    /**
     * Generate a password assertion ID.
     * @param user
     * @return
     */
    protected String generateAssertionId(SSOUser user)  throws PasswordManagementException {
        if (log.isDebugEnabled())
            log.debug("Generating assertion ID for " + user.getName());

        return this.idGenerator.generateId();
    }

    /**
     * After password is reseted. This method distributes the new value.
     * @param user The user information.
     * @param password The password in clear text!
     */
    protected void distribute(SSOUser user, String password) throws PasswordManagementException {

        if (log.isDebugEnabled())
            log.debug("Distributing password for " + user.getName());

        // Add all necessary properties for

        this.distributor.distributePassword(user, password, this.getLostPasswordState());
    }

    protected SSOUser findUserByUsername(String username) {
        if (log.isDebugEnabled())
            log.debug("Looking for user " + username);

        try {

            return identityManager.findUser(username);
        } catch (SSOIdentityException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    // -------------------------------------------------------------------------< Some utils >


    protected LostPasswordProcessState getLostPasswordState() {
        return (LostPasswordProcessState) getState();
    }

    protected ChallengeResponseCredential getChallenge(String id) {
        return getChallenge(id, retrieveAllChallenges());
    }

    protected ChallengeResponseCredential getChallenge(String id, Set<ChallengeResponseCredential> challenges) {
        for (ChallengeResponseCredential challenge : challenges) {
            if (challenge.getId().equals(id))
                return challenge;
        }
        return null;
    }

    protected void storeAllChallenges(ChallengeResponseCredential[] challenges) {

        if (challenges == null)
            return;

        for (int i = 0; i < challenges.length; i++) {
            ChallengeResponseCredential challenge = challenges[i];
            getLostPasswordState().getChallenges().add(challenge);

            if (log.isDebugEnabled())
                log.debug("Storing challenge : " + challenge.getId() + " ["+challenge.getResponse()+"]");
        }
    }

    protected void clearChallenges() {
        getLostPasswordState().getChallenges().clear();
    }

    protected Set<ChallengeResponseCredential> retrieveAllChallenges() {
        return getLostPasswordState().getChallenges();
    }

    public String getPasswordAssertionId() {
        return getLostPasswordState().getAssertionId();
    }

    /**
     * @org.apache.xbean.Property alias="credential-provider"
     *
     * @return
     */
    public CredentialProvider getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }


    /**
     * @org.apache.xbean.Property alias="password-distributor"
     * @return
     */
    public PasswordDistributor getPasswordDistributor() {
        return distributor;
    }

    public void setPasswordDistributor(PasswordDistributor distributor) {
        this.distributor = distributor;
    }

    /**
     * @org.apache.xbean.Property alias="password-generator"
     * @return
     */
    public PasswordGenerator getPasswordGenerator() {
        return generator;
    }

    public void setPasswordGenerator(PasswordGenerator generator) {
        this.generator = generator;
    }

    /**
     * @org.apache.xbean.Property alias="assertion-generator"
     * @return
     */
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * @org.apache.xbean.Property alias="identity-manager"
     *
     * @return
     */
    public SSOIdentityManager getIdentityManager() {
        return identityManager;
    }

    public void setIdentityManager(SSOIdentityManager identityManager) {
        this.identityManager = identityManager;
    }


}
