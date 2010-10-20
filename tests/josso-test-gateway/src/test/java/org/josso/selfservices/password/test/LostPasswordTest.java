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

package org.josso.selfservices.password.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.josso.selfservices.password.*;
import org.josso.selfservices.password.lostpassword.Constants;
import org.josso.selfservices.password.lostpassword.AbstractLostPasswordProcess;
import org.josso.selfservices.password.lostpassword.LostPasswordUrlProvider;
import org.josso.selfservices.password.lostpassword.LostPasswordProcessState;
import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.selfservices.ProcessRequest;
import org.josso.selfservices.ProcessResponse;
import org.josso.selfservices.ProcessState;
import org.josso.gateway.identity.SSOUser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: LostPasswordTest.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class LostPasswordTest {

    private static Log log = LogFactory.getLog(LostPasswordTest.class);

    protected ApplicationContext applicationContext;

    @Before
    public void initAppContext() {
        applicationContext = new ClassPathXmlApplicationContext("/org/josso/selfservices/password/test/josso-lostpassword-spring.xml");
    }

    @Test
    public void testLostPassword() throws Exception {


        PasswordManagementService svc = (PasswordManagementService) applicationContext.getBean("josso-password-manager");
        MockPasswordDistributor distributor = new MockPasswordDistributor();
        assert svc instanceof PasswordManagementServiceImpl : "Unknonw Password Management Service";

        for (PasswordManagementProcess p  : ((PasswordManagementServiceImpl)svc).getPrototypeProcesses()) {
            ((AbstractLostPasswordProcess)p).setPasswordDistributor(distributor);
        }

        ProcessResponse response = svc.startProcess("josso-simple-lostpasswordprocess");
        assert response != null : "No response recieved";

        // Register a runtime extension
        LostPasswordUrlProvider mockLostPaswordUrlProvider = new LostPasswordUrlProvider() {
            public String provideResetUrl(String passwordAssertionId) {
                return "http://my-test-url/assertionId=" + passwordAssertionId;
            }
        };
        svc.register(response.getProcessId(), Constants.EXT_URL_PROVIDER, mockLostPaswordUrlProvider);

        // Execute process
        int i = 0;
        String nextStep = response.getNextStep();

        while (nextStep != null) {

            assert i < 20 : "Process is too long, more than 20 steps!";

            ProcessRequest request = svc.createRequest(response.getProcessId());
            log.debug("Processing STEP : " + nextStep);

            if (nextStep.equals(Constants.STEP_REQUEST_CHALLENGES)) {

                log.debug("Providing E-Mail Challenge");

                // Provide Email
                ChallengeResponseCredential[] challenges = (ChallengeResponseCredential[]) response.getAttribute(Constants.ATTR_CHALLENGES);

                assert challenges != null : "No challenges requested";
                assert challenges.length == 1: "Invalid number of challenges " + challenges.length;
                ChallengeResponseCredential email = challenges[0];

                assert email.getId().equals("email") : "Unknown challeng type " + email.getId();
                email.setResponse("user1@josso.org");
                
                request.setAttribute(Constants.ATTR_CHALLENGES, challenges);

            } else if (nextStep.equals(Constants.STEP_CONFIRM_PASSWORD)) {

                log.debug("Providing Password Assertion Challenge");

                ChallengeResponseCredential[] challenges = (ChallengeResponseCredential[]) response.getAttribute(Constants.ATTR_CHALLENGES);

                assert challenges != null : "No confirmaiton challenges requested";
                assert challenges.length == 1: "Invalid number of confirmation challenges " + challenges.length;
                ChallengeResponseCredential assertion = challenges[0];

                assert assertion.getId().equals(Constants.CHALLENGE_PWD_ASSERTION_ID) : "Unknown challeng type " + assertion.getId();
                assertion.setResponse(distributor.getAssertionId());

                log.debug("Responding challeng " + Constants.CHALLENGE_PWD_ASSERTION_ID + " with " + distributor.getAssertionId());

                request.setAttribute(Constants.ATTR_CHALLENGES, challenges);
            }

            response = svc.handleRequest(request);
            nextStep = response.getNextStep();

            if (nextStep != null) {
                assert !nextStep.equals(Constants.STEP_AUTH_ERROR) : "AUTH ERROR while resetting password";
                assert !nextStep.equals(Constants.STEP_FATAL_ERROR) : "FATAL ERROR while resetting password : ";
            }

            i++;
        }


    }


    private class MockPasswordDistributor implements PasswordDistributor {

        private String assertionId;

        public void distributePassword(SSOUser user, String clearPassword, ProcessState state) {

            log.debug("Distributing password["+clearPassword+"] for user ["+user.getName()+"] using assertion : " + state.getAttribute("assertionId"));
            this.assertionId = ((LostPasswordProcessState)state).getAssertionId();
        }

        public String getAssertionId() {
            return assertionId;
        }
    }

}
