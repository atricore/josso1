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
package org.josso.gateway.signon;

/**
 * Some constants used by frontchannel http.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: Constants.java 1607 2010-05-11 13:39:08Z sgonzalez $
 */

public interface Constants extends org.josso.gateway.Constants {

    /**
     * Reqeuest parameter representing an SSO command.
     * Value : sso_cmd
     */
    public static final String PARAM_JOSSO_CMD = "josso_cmd";

    /**
     * Request parameter to set the back-to url used after a successfull login.
     */
    public static final String PARAM_JOSSO_BACK_TO = "josso_back_to";

    /**
     * Request parameter to set the on-error to url used after an invalid login.
     */
    public static final String PARAM_JOSSO_ON_ERROR = "josso_on_error";

    /**
     * Request parameter to set for notifying the relaying profile
     */
    public static final String PARAM_RELAY_PROFILE = "josso_relay_profile";


    /**
     * Partner application ID parameter
     */
    public static final String PARAM_JOSSO_PARTNERAPP_ID = "josso_partnerapp_id";


    /**
     * Partner application host parameter
     */
    public static final String PARAM_JOSSO_PARTNERAPP_HOST = "josso_partnerapp_host";


    /**
     * Partner application context parameter
     */
    public static final String PARAM_JOSSO_PARTNERAPP_CONTEXT = "josso_partnerapp_ctx";

    /**
     * 'Remember Me' request parameter
     */
    public static final String PARAM_JOSSO_REMEMBERME = "josso_rememberme";


    /**
     * Key to store a String representing the URL were the user should be redirected to after a successfull login.
     */
    public static final String KEY_JOSSO_BACK_TO = "org.josso.gateway.backToUrl";


    /**
     * Key to store a String representing the URL were the user should be redirected to after an invalid login attempt
     */
    public static final String KEY_JOSSO_ON_ERROR = "org.josso.gateway.onErrorUrl";

    /**
     * Key used to store the SSOGateway instance in the application context.
     * Value : org.josso.gateway
     */
    public static final String KEY_JOSSO_GATEWAY = "org.josso.gateway";

    /**
     * Key to store a SSOUser instance in any scope.
     */
    public static final String KEY_JOSSO_USER = "org.josso.gateway.user";

    /**
     * Key to store a SSORole[] instance in any scope.
     */
    public static final String KEY_JOSSO_USER_ROLES = "org.josso.gateway.userRoles";
    /**
     * Key to store a SSOUser instance in any scope.
     */
    public static final String KEY_JOSSO_SESSION = "org.josso.gateway.session";

    /**
     * Key to store a String representing the relay profile
     */
    public static final String KEY_JOSSO_RELAY_PROFILE = "org.josso.gateway.relayProfile";

    /**
     * Key to store a String representing the name of a SSO Security Domain
     */
    public static final String KEY_JOSSO_SECURITY_DOMAIN_NAME = "org.josso.gateway.securityDomainName";


    /**
     * Identifier for inbound relay profile
     */
    public static final String INBOUND_RELAY_PROFILE = "inbound";

    /**
     * Identifier for outbound relay profile
     */
    public static final String OUTBOUND_RELAY_PROFILE = "outbound";


}
