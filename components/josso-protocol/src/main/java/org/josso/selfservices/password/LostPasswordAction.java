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

package org.josso.selfservices.password;

import org.apache.struts.action.*;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.josso.gateway.SSOContext;
import org.josso.gateway.SSOException;
import org.josso.selfservices.password.lostpassword.Constants;
import org.josso.selfservices.password.lostpassword.LostPasswordProcessState;
import org.josso.selfservices.password.lostpassword.LostPasswordUrlProvider;
import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.selfservices.ProcessRequest;
import org.josso.selfservices.ProcessResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: LostPasswordAction.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class LostPasswordAction extends SelfServicesBaseAction {

    public static final String JOSSO_CMD_CONFIRM_PASSWORD = "confirmPwd";

    public static final String JOSSO_CMD_LOST_PASSWORD = "lostPwd";

    public static final String PARAM_JOSSO_CMD = "josso_cmd";

    public static final String PARAM_JOSSO_PROCESS_ID = "josso_pidId";

    public static final String ATTR_LAST_PROCESS_RESPONSE = "org.josso.selfservices.lostpassword.processResponse";

    private static Log log = LogFactory.getLog(LostPasswordAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // We need the seccurity domain !!!
        prepareContext(request);

        String josso_cmd = getSSOCmd(request);

        SSOContext ctx = SSOContext.getCurrent();
        PasswordManagementService pwdService = ctx.getSecurityDomain().getPasswordManager();

        HttpSession session = request.getSession();

        // ProcessResponse previousProcessResponse = null;


        try {

            String processId = null;

            // We are starting a new lost password process.
            if (josso_cmd != null && josso_cmd.equals(JOSSO_CMD_LOST_PASSWORD)) {

                log.debug("Initializing lost password process");
                ProcessResponse pr = pwdService.startProcess("josso-simple-lostpassword");
                processId = pr.getProcessId();

                // Register a new url provider for this process
                final String pid = pr.getProcessId();
                final String baseUrl = request.getScheme() + "://" + request.getHeader("Host") +
                        request.getContextPath() + mapping.findForward("requestConfirmPassword").getPath() +
                        "?josso_cmd=" + JOSSO_CMD_CONFIRM_PASSWORD;

                // This will create a URL that will 
                LostPasswordUrlProvider lostPaswordUrlProvider = new LostPasswordUrlProvider() {
                    public String provideResetUrl(String passwordAssertionId) {
                        return baseUrl + "&" + PARAM_JOSSO_PROCESS_ID + "=" + pid + "&" + "josso_" + Constants.CHALLENGE_PWD_ASSERTION_ID + "=" + passwordAssertionId;
                    }
                };

                pwdService.register(processId, Constants.EXT_URL_PROVIDER, lostPaswordUrlProvider);

                // Store process state and render view
                log.debug("Process First Step (forward) : " + pr.getNextStep());
                session.setAttribute(ATTR_LAST_PROCESS_RESPONSE, pr);

                return mapping.findForward(pr.getNextStep());

            }

            // We are processing a request for a running process.
            ProcessResponse pr = (ProcessResponse) session.getAttribute(ATTR_LAST_PROCESS_RESPONSE);
            if (pr == null) {
                processId = request.getParameter(PARAM_JOSSO_PROCESS_ID);
                if (log.isDebugEnabled())
                    log.debug("Using process id ["+processId+"] from session");

            } else {

                processId = pr.getProcessId();

                if (log.isDebugEnabled())
                    log.debug("Using process id ["+processId+"] from request");
            }

            // Move the process to the next step !

            ProcessRequest processRequest = pwdService.createRequest(processId);
            ChallengeResponseCredential[] challenges = fillChallengeResponses((LostPasswordProcessState)pwdService.getProcessState(processId), form, request);
            processRequest.setAttribute(Constants.ATTR_CHALLENGES, challenges);

            ProcessResponse processResponse = pwdService.handleRequest(processRequest);
            session.setAttribute(ATTR_LAST_PROCESS_RESPONSE, processResponse);

            log.debug("Process Next Step (forward) : " + processResponse.getNextStep() + ". Final " + processResponse.isNextStepFinal());

            if (processResponse.getNextStep().equals(Constants.STEP_FATAL_ERROR) && processResponse.getAttribute("error") != null) {
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("sso.error", ((Exception)processResponse.getAttribute("error")).getMessage() ));
                saveErrors(request, errors);

            }

            return mapping.findForward(processResponse.getNextStep());


        } catch (Exception e) {

            log.error("Error recovering password : " + e.getMessage(), e);
            request.setAttribute("error", e);

            // logs the error
            ActionErrors errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("sso.error", e.getMessage()));
            saveErrors(request, errors);

            return mapping.findForward("fatalError");
        }

    }


    protected ChallengeResponseCredential[] fillChallengeResponses(LostPasswordProcessState state, ActionForm form, HttpServletRequest request) {

        List<ChallengeResponseCredential> respondedChallenges = new ArrayList<ChallengeResponseCredential>();

        // Check request parameter values:
        HttpSession session = request.getSession();
        ProcessResponse previousProcessResponse = (ProcessResponse) session.getAttribute(ATTR_LAST_PROCESS_RESPONSE);

        Set<ChallengeResponseCredential> challenges = state.getChallenges();
        if (challenges == null || challenges.size() == 0) {
            if (log.isDebugEnabled())
                log.debug("No challenges requested");

            return null;
        }

        // Fill challenges
        for (ChallengeResponseCredential challenge : state.getChallenges()) {

            String challengeId = challenge.getId();

            // Try to recover challenge response from http request parameter
            String challengeResponse = request.getParameter(challengeId);
            if (challengeResponse == null) {
                // try with the prefix
                challengeResponse = request.getParameter("josso_" + challengeId);
            }

            // Try to recover challenge response from DynaActionForm

            if (challengeResponse == null && form instanceof DynaActionForm ) {
                DynaActionForm dForm = (DynaActionForm) form;
                try { challengeResponse = (String) dForm.get(challengeId); } catch (IllegalArgumentException e) { log.debug("Form does not have field " + challengeId ); }
                if (challengeResponse == null)
                    try { challengeResponse = (String) dForm.get("josso_" + challengeId); } catch (IllegalArgumentException e) { log.debug("Form does not have field " + challengeId ); }
            }

            if (challengeResponse != null) {
                if (log.isDebugEnabled())
                    log.debug("Found response for challenge : " + challengeId);
                challenge.setResponse(challengeResponse);
                respondedChallenges.add(challenge);
            }
        }

        return respondedChallenges.toArray(new ChallengeResponseCredential[respondedChallenges.size()]);

    }

    /**
     * Gets the received SSO Command. If command is empty (""), returns null.
     */
    protected String getSSOCmd(HttpServletRequest request) {
        String cmd = request.getParameter(PARAM_JOSSO_CMD);
        if ("".equals(cmd))
            cmd = null;
        return cmd;
    }

}
