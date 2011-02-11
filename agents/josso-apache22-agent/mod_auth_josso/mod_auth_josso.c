/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2008, Atricore, Inc.
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
 */

#include "soapH.h"

#include "SSOIdentityManagerSOAPBinding.nsmap"

#include "apr_strings.h"
#include "apr_md5.h"            /* for apr_password_validate */
#include "apr_lib.h"            /* for apr_isspace */
#include "apr_base64.h"         /* for apr_base64_decode et al */
#define APR_WANT_STRFUNC        /* for strcasecmp */
#include "apr_want.h"
#include "apr_hash.h"

#include "ap_config.h"
#include "httpd.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "http_protocol.h"
#include "http_request.h"
#include "ap_provider.h"

#include "mod_auth.h"

#include "regex.h"

typedef struct {
    char *dir;
    char *gatewayLoginUrl;
    char *gatewayLogoutUrl;
    char *gatewayEndpointHost;
    int   gatewayEndpointPort;
    char *sessionManagerServicePath;
    char *identityManagerServicePath;
    char *identityProviderServicePath;
	char *partnerAppId;
    char *context;
    char *defaultResource;
    char *sessionAccessMinInterval;
    char *customAuthType;
    int   PHP5SecurityContext;
    /* SOAP SSL options */
    int   GatewayEndpointSSLEnable;
    int   EnableGatewayAuthentication;
    char *sslServerCertFile;
    char *sslServerCertDir;
    //char *sslClientKeyFile;
    //char *sslClientKeyFilePass;
    //char *sslRandFile;
} auth_josso_config_rec;

typedef struct {
    char *id;
    char *name;
    char *cover_url;
	char *details_url;
	char *owner_name;
	char *owner_url;
	char *owner_email;
	char *status;
	char *purpose;
	char *type;
	char *platform;
	char *availability;
	char *exclusion;
	char *exclusion_useragent;
	char *noindex;
	char *host;
	char *from;
	char *user_agent;
	char *language;
	char *description;
	char *history;
	char *environment;
	char *modified_date;
	char *modified_by;
} robot;

typedef struct {
	char *strategy;
	char *mode;
} base_auto_login_rec;

typedef struct {
	base_auto_login_rec base;
	char *url_patterns[20];
	int url_patterns_size;
} urlbased_auto_login_rec;

typedef struct {
	base_auto_login_rec base;
	char *bots_file;
	apr_hash_t *robots;
} bot_auto_login_rec;

void **automaticStrategies;
int automaticStrategiesSize = 0;

static ap_regex_t *robot_regexps[24];
static int robot_regexps_compiled = 0;

#define JOSSO_LOGIN_URI "/josso_login/"
#define JOSSO_SECURITY_CHECK_URI "/josso_security_check"
#define JOSSO_LOGOUT_URI "/josso_logout/"
#define JOSSO_ASSERTION_ID_PARAMETER "josso_assertion_id"
#define JOSSO_SINGLE_SIGN_ON_COOKIE "JOSSO_SESSIONID"
#define JOSSO_PROTECTED_RESOURCE_COOKIE "JOSSO_PROTECTED_RESOURCE"
#define JOSSO_ROLE_NOTE "josso_role_note"
#define JOSSO_AUTOMATIC_LOGIN_EXECUTED "JOSSO_AUTOMATIC_LOGIN_EXECUTED"
#define JOSSO_AUTOMATIC_LOGIN_REFERER "JOSSO_AUTOMATIC_LOGIN_REFERER"
#define DEFAULT_SESSION_MANAGER_SERVICE_PATH "/josso/services/SSOSessionManagerSoap"
#define DEFAULT_IDENTITY_MANAGER_SERVICE_PATH "/josso/services/SSOIdentityManagerSoap"
#define DEFAULT_IDENTITY_PROVIDER_SERVICE_PATH "/josso/services/SSOIdentityProviderSoap"

#define JOSSO_DEFAULT_AUTH_LOGIN_STRATEGY "DEFAULT"
#define JOSSO_URLBASED_AUTH_LOGIN_STRATEGY "URLBASED"
#define JOSSO_BOT_AUTH_LOGIN_STRATEGY "BOT"

#define JOSSO_AUTH_LOGIN_REQUIRED "REQUIRED"
#define JOSSO_AUTH_LOGIN_SUFFICIENT "SUFFICIENT"
#define JOSSO_AUTH_LOGIN_OPTIONAL "OPTIONAL"

static char *resolveAssertion(request_rec *r, const char* assertionId);
static char *form_value(apr_pool_t *pool, apr_hash_t *form, const char *key);
static apr_hash_t *parse_form_from_string(request_rec *r, char *args);
static apr_hash_t *parse_form_from_GET(request_rec *r);
static char *get_cookie (request_rec * r, char *name);
static char* findUserInSession(request_rec *r, char *sessionId);
static apr_array_header_t* findRolesBySSOSessionId(request_rec *r, char *sessionId);
static char *set_session_cookie (request_rec * r, char *name, char *value, char *path, char *domain);
static char *generate_uuid(request_rec * r, int length);
static void accessSession(request_rec *r, char *sessionId);
static const char *getSSOIdentityManagerServiceEndpoint(request_rec *r);
static const char *getSSOSessionManagerServiceEndpoint(request_rec *r);
static const char *getSSOIdentityProviderServiceEndpoint(request_rec *r);
static void createPHP5SecurityContext(request_rec *r);
static char *remove_session_cookie(request_rec *r, char *name, char *path, char *domain);
static int isAutomaticLoginRequired(request_rec *r);
static const char *getHost(request_rec *r);
static const char *createBaseUrl(request_rec *r);
static const char *getRequestedUrl(request_rec *r, int includeParameters);
static const char *getContextPath(request_rec *r);
static int isPublicResource(request_rec *r);
static void prepareNonCacheResponse(request_rec *r);
static const char *getGatewayEndpointScheme(request_rec *r);
static int setupSSLContext(struct soap *soap, request_rec *r);
static int isAutoLoginRequired(int strategyIndex, request_rec *r);
static int isDefaultAutoLoginRequired(base_auto_login_rec *default_auto_login, request_rec *r);
static int isUrlBasedAutoLoginRequired(urlbased_auto_login_rec *urlbased_auto_login, request_rec *r);
static int isBotAutoLoginRequired(bot_auto_login_rec *bot_auto_login, request_rec *r);
static char *appendChar(char *value, char ch);
static int loadRobots(bot_auto_login_rec *bot_auto_login);
static int setRobotProperty(robot *robot, char *name, char *value, int append, apr_pool_t *mp);
static void compileRobotRegexp(apr_pool_t *mp);
static const char *getRequester(request_rec *r);

module AP_MODULE_DECLARE_DATA auth_josso_module;


static void *create_auth_josso_dir_config(apr_pool_t *p, char *d)
{
    auth_josso_config_rec *conf = apr_pcalloc(p, sizeof(*conf));

    conf->dir = d;
    conf->sessionManagerServicePath = DEFAULT_SESSION_MANAGER_SERVICE_PATH;
    conf->identityManagerServicePath = DEFAULT_IDENTITY_MANAGER_SERVICE_PATH;
    conf->identityProviderServicePath = DEFAULT_IDENTITY_PROVIDER_SERVICE_PATH;

    return conf;
}

static const char *set_gateway_login_url_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_gateway_logout_url_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_gateway_endpoint_slot(cmd_parms *cmd, void *offset,
                                       const char *h, const char* p)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    conf->gatewayEndpointHost = h;
    conf->gatewayEndpointPort = atoi(p);

    return NULL;
}

static const char *set_session_manager_path_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;
    
    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_identity_manager_path_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_identity_provider_path_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_partner_app_id_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_context_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_default_resource_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_session_access_min_interval_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_custom_auth_type_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_php5_security_context_slot(cmd_parms *cmd, void *offset,
                                       int u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_flag_slot(cmd, offset, u);
}

static const char *set_ssl_enable_slot(cmd_parms *cmd, void *offset,
                                       int u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_flag_slot(cmd, offset, u);
}

static const char *set_enable_gateway_auth_slot(cmd_parms *cmd, void *offset,
                                       int u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_flag_slot(cmd, offset, u);
}

static const char *set_ssl_server_cert_file_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

static const char *set_ssl_server_cert_dir_slot(cmd_parms *cmd, void *offset,
                                       const char *u)
{
    auth_josso_config_rec *conf = (auth_josso_config_rec*)offset;

    return ap_set_string_slot(cmd, offset, u);
}

/*
 * example: DefaultAutoLoginStrategy "REQUIRED"
 */
static const char *set_default_login_strategy_slot(cmd_parms *cmd, void *offset,
                                       const char *mode)
{
	if (automaticStrategiesSize == 0) {
		automaticStrategies = malloc(10 * sizeof *automaticStrategies);
    }

	base_auto_login_rec *default_auto_login = malloc(sizeof(base_auto_login_rec));
	default_auto_login->strategy = JOSSO_DEFAULT_AUTH_LOGIN_STRATEGY;
	default_auto_login->mode = mode;

    automaticStrategies[automaticStrategiesSize] = default_auto_login;
    automaticStrategiesSize++;

    return NULL;
}

/*
 * example: UrlBasedAutoLoginStrategy "OPTIONAL" "pattern1,pattern2"
 */
static const char *set_urlbased_login_strategy_slot(cmd_parms *cmd, void *offset,
                                       const char *mode, const char *url_patterns)
{
	if (automaticStrategiesSize == 0) {
        automaticStrategies = malloc(10 * sizeof *automaticStrategies);
    }

	urlbased_auto_login_rec *urlbased_auto_login = malloc(sizeof(urlbased_auto_login_rec));
	urlbased_auto_login->base.strategy = JOSSO_URLBASED_AUTH_LOGIN_STRATEGY;
	urlbased_auto_login->base.mode = mode;
	urlbased_auto_login->url_patterns_size = 0;

	char *token = NULL;
    token = strtok(url_patterns, ",");
    while (token != NULL) {
        urlbased_auto_login->url_patterns[urlbased_auto_login->url_patterns_size] = token;
        urlbased_auto_login->url_patterns_size++;
        apr_collapse_spaces(token, token);
        token = strtok(NULL, ",");
    }
    automaticStrategies[automaticStrategiesSize] = urlbased_auto_login;
    automaticStrategiesSize++;

    return NULL;
}

/*
 * example: BotAutoLoginStrategy "REQUIRED" "/path/to/bots.file"
 */
static const char *set_bot_login_strategy_slot(cmd_parms *cmd, void *offset,
                                       const char *mode, const char *bots_file)
{
	if (automaticStrategiesSize == 0) {
        automaticStrategies = malloc(10 * sizeof *automaticStrategies);
    }

	bot_auto_login_rec *bot_auto_login = malloc(sizeof(bot_auto_login_rec));
	bot_auto_login->base.strategy = JOSSO_BOT_AUTH_LOGIN_STRATEGY;
	bot_auto_login->base.mode = mode;
	if (bots_file != NULL) {
		bot_auto_login->bots_file = bots_file;
		loadRobots(bot_auto_login);
	}
	automaticStrategies[automaticStrategiesSize] = bot_auto_login;
    automaticStrategiesSize++;

    return NULL;
}

static const command_rec auth_josso_cmds[] =
{
   AP_INIT_TAKE1("GatewayLoginUrl", set_gateway_login_url_slot,
                   (void *)APR_OFFSETOF(auth_josso_config_rec, gatewayLoginUrl),
                   OR_AUTHCFG, "JOSSO Login Redirect URL"),
   AP_INIT_TAKE1("GatewayLogoutUrl", set_gateway_logout_url_slot,
				  (void *)APR_OFFSETOF(auth_josso_config_rec, gatewayLogoutUrl),
				  OR_AUTHCFG, "JOSSO Logout Redirect URL"),
   AP_INIT_TAKE2("GatewayEndpoint", set_gateway_endpoint_slot,
				  NULL,
				  OR_AUTHCFG, "Gateway Endpoint for Back Channel Communication"),
   AP_INIT_TAKE1("SessionManagerServicePath", set_session_manager_path_slot,
                   (void *)APR_OFFSETOF(auth_josso_config_rec, sessionManagerServicePath),
                   OR_AUTHCFG, "SSOSessionManager service path"),
   AP_INIT_TAKE1("IdentityManagerServicePath", set_identity_manager_path_slot,
                   (void *)APR_OFFSETOF(auth_josso_config_rec, identityManagerServicePath),
                   OR_AUTHCFG, "SSOIdentityManager service path"),
   AP_INIT_TAKE1("IdentityProviderServicePath", set_identity_provider_path_slot,
                   (void *)APR_OFFSETOF(auth_josso_config_rec, identityProviderServicePath),
                   OR_AUTHCFG, "SSOIdentityProvider service path"),
   AP_INIT_TAKE1("PartnerApplicationID", set_partner_app_id_slot,
                   (void *)APR_OFFSETOF(auth_josso_config_rec, partnerAppId),
                   OR_AUTHCFG, "Partner application ID"),
   AP_INIT_TAKE1("Context", set_context_slot,
				  (void *)APR_OFFSETOF(auth_josso_config_rec, context),
				  OR_AUTHCFG, "Partner application context"),
   AP_INIT_TAKE1("DefaultResource", set_default_resource_slot,
				  (void *)APR_OFFSETOF(auth_josso_config_rec, defaultResource),
				  OR_AUTHCFG, "Default resource to send the user after logout"),
   AP_INIT_TAKE1("SessionAccessMinInterval", set_session_access_min_interval_slot,
				  (void *)APR_OFFSETOF(auth_josso_config_rec, sessionAccessMinInterval),
				  OR_AUTHCFG, "Session Access Minimum Interval for SSO Session Keep Alive"),
   AP_INIT_TAKE1("CustomAuthType", set_custom_auth_type_slot,
				  (void *)APR_OFFSETOF(auth_josso_config_rec, customAuthType),
				  OR_AUTHCFG, "Custom Value for the AUTH_TYPE request parameter"),
   AP_INIT_FLAG("PHP5SecurityContext", set_php5_security_context_slot,
				 (void *)APR_OFFSETOF(auth_josso_config_rec, PHP5SecurityContext),
				 OR_AUTHCFG,
				 "Set to 'On' to create PHP5-specific security context using "
				 "PHP_AUTH_USER, PHP_AUTH_PW and PHP_AUTH_TYPE server variables"
				 "(default is Off)."),
   AP_INIT_TAKE1("DefaultAutoLoginStrategy", set_default_login_strategy_slot,
				  NULL, OR_AUTHCFG, "Default automatic login strategy"),
   AP_INIT_TAKE2("UrlBasedAutoLoginStrategy", set_urlbased_login_strategy_slot,
				  NULL, OR_AUTHCFG, "Url based automatic login strategy"),
   AP_INIT_TAKE2("BotAutoLoginStrategy", set_bot_login_strategy_slot,
				  NULL, OR_AUTHCFG, "Bot automatic login strategy"),
   AP_INIT_FLAG("GatewayEndpointSSLEnable", set_ssl_enable_slot,
				 (void *)APR_OFFSETOF(auth_josso_config_rec, GatewayEndpointSSLEnable),
				 OR_AUTHCFG,
				 "Set to 'On' to enable secure SOAP clients with HTTPS/SSL "
				 "(default is Off)."),
   AP_INIT_FLAG("EnableGatewayAuthentication", set_enable_gateway_auth_slot,
				 (void *)APR_OFFSETOF(auth_josso_config_rec, EnableGatewayAuthentication),
				 OR_AUTHCFG,
				 "Set to 'On' to enable gateway authentication "
				 "(default is Off)."),
   AP_INIT_TAKE1("SSLServerCertFile", set_ssl_server_cert_file_slot,
			     (void *)APR_OFFSETOF(auth_josso_config_rec, sslServerCertFile),
			     OR_AUTHCFG, "Path to server certificate file "
			     "that stores trusted certificates (needed to verify server)"),
   AP_INIT_TAKE1("SSLServerCertDir", set_ssl_server_cert_dir_slot,
			     (void *)APR_OFFSETOF(auth_josso_config_rec, sslServerCertDir),
			     OR_AUTHCFG, "Path to the directory with trusted certificates"),
   /* AP_INIT_TAKE1("SSLClientKeyFile", set_ssl_client_key_file_slot,
	*			 (void *)APR_OFFSETOF(auth_josso_config_rec, sslClientKeyFile),
	*			 OR_AUTHCFG, "Path to client key file. "
	*			 "required only when client must authenticate to server "
	*			 "(see SSL docs on how to obtain this file)"),
    * AP_INIT_TAKE1("SSLClientKeyFilePass", set_ssl_client_key_file_pass_slot,
	*			 (void *)APR_OFFSETOF(auth_josso_config_rec, sslClientKeyFilePass),
	*			 OR_AUTHCFG, "Password to read the client key file"),
    * AP_INIT_TAKE1("SSLRandFile", set_ssl_rand_file_slot,
	*		     (void *)APR_OFFSETOF(auth_josso_config_rec, sslRandFile),
	*		     OR_AUTHCFG, "File with random data to seed randomness"),
    */
  {NULL}

};

/* Determine user ID, and check if it really is that user, for HTTP
 * basic authentication...
 */
static int authenticate_josso_user(request_rec *r)
{
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);

	const char* location;
	const char* jossoSessionId;
	const char* username;
	const apr_array_header_t* roles;

	const char *path = getContextPath(r);

	/* Check if the partner application required a logout */
	if ( strstr(r->unparsed_uri, JOSSO_LOGOUT_URI) ) {
		const char *baseUrl = createBaseUrl(r);
		const char *backToPath = cfg->defaultResource;
		if (backToPath == NULL) {
			backToPath = path;
		}
		location = apr_psprintf(r->pool, "%s?josso_back_to=%s%s",
			cfg->gatewayLogoutUrl,
			baseUrl,
			backToPath
		);

		remove_session_cookie(r, JOSSO_SINGLE_SIGN_ON_COOKIE, path, NULL);

		prepareNonCacheResponse(r);
		apr_table_setn(r->err_headers_out, "Location", location);
		ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
					"User logout: [%s%s]", baseUrl, backToPath);
		return HTTP_MOVED_TEMPORARILY;
	}

	/* If no authentication is wanted, it's none of our business */
	if (!ap_some_auth_required(r)) {
		return DECLINED;
	}

	/* The Gateway is relaying to us an assertion reference which it must be resolved */
	if ( strstr(r->unparsed_uri, JOSSO_SECURITY_CHECK_URI) ) {
		apr_hash_t *formdata = NULL;
		char  *assertionId;

		formdata = parse_form_from_GET(r);
		assertionId = form_value(r->pool, formdata, JOSSO_ASSERTION_ID_PARAMETER);
		if (assertionId != NULL) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "resolving assertion [%s]", assertionId);
			jossoSessionId = resolveAssertion(r, assertionId);

			if (jossoSessionId != NULL) {
				ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "assertion resolved to session identifier [%s]", jossoSessionId);
				set_session_cookie(r, JOSSO_SINGLE_SIGN_ON_COOKIE, jossoSessionId, path, NULL);

				const char *originalUrl = get_cookie(r, JOSSO_PROTECTED_RESOURCE_COOKIE);
				prepareNonCacheResponse(r);
				ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "redirecting to original url [%s]", originalUrl);
				apr_table_setn(r->err_headers_out, "Location", originalUrl);

				remove_session_cookie(r, JOSSO_PROTECTED_RESOURCE_COOKIE, path, NULL);

				return HTTP_MOVED_TEMPORARILY;
			} else {
				ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Outbound relaying failed for assertion id [%s], no Principal found.", assertionId);
				return HTTP_FORBIDDEN;
			}
		} else {
			const char *originalUrl = get_cookie(r, JOSSO_PROTECTED_RESOURCE_COOKIE);
			prepareNonCacheResponse(r);
			apr_table_setn(r->err_headers_out, "Location", originalUrl);
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "assertion is null, redirecting to original url [%s]", originalUrl);
			return HTTP_MOVED_TEMPORARILY;
		}
	} else {
		jossoSessionId = get_cookie(r, JOSSO_SINGLE_SIGN_ON_COOKIE);
	}

	/* SSO session present, obtain user and roles information and use it for creating the security context */
	if (jossoSessionId != NULL) {
		ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "obtaining user for SSO session [%s]", jossoSessionId );
		username = findUserInSession(r, jossoSessionId);

		if (username != NULL) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "obtained user [%s] for session [%s]",
						  username, jossoSessionId);
		} else {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "there is no user for session [%s]",
						  username);
			// remove invalid cookie
			remove_session_cookie(r, JOSSO_SINGLE_SIGN_ON_COOKIE, path, NULL);
		}

		if (username != NULL) {
			roles = findRolesBySSOSessionId(r, jossoSessionId);

			int i;
			for (i = 0; i < roles->nelts; i++ ) {
				ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "found role [%s] for user [%s]",
							  ((char **)roles->elts)[i],
							  username);
			}

			// create security context
			r->user = username;

			// propagate JOSSO as the authentication mechanism used or use a custom value in case
			// one is configured.
			if (cfg->customAuthType == NULL)
				r->ap_auth_type = (char *) "JOSSO";
			else
				r->ap_auth_type = cfg->customAuthType;

			if (roles != NULL)
				apr_table_setn(r->notes, JOSSO_ROLE_NOTE, roles);

			// create PHP5-specific security context if enabled
			if (cfg->PHP5SecurityContext == 1)
				createPHP5SecurityContext(r);

			// keeping session alive
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Notifying keep alive for SSO session [%s]", jossoSessionId );
			accessSession(r, jossoSessionId);
		}

	}
	/* There is no security context present for the user so redirect him to the authentication form */
	if (r->user == NULL) {
		// the resource requires an SSO session so redirect the user for authentication
		const char *host = getHost(r);

		//TODO: convey protocol and query string
		const char *requestedUrl = getRequestedUrl(r, 1);

		set_session_cookie(r, JOSSO_PROTECTED_RESOURCE_COOKIE, requestedUrl, path, NULL);

		int requireLogin = 1;

		if (jossoSessionId != NULL || isAutomaticLoginRequired(r)) {
			location = apr_psprintf(r->pool, "%s?josso_cmd=login_optional&josso_partnerapp_host=%s&josso_partnerapp_id=%s&josso_partnerapp_ctx=%s&josso_back_to=%s%s",
							   cfg->gatewayLoginUrl,
							   host,
							   getRequester(r),
							   r->uri,
							   getRequestedUrl(r, 0),
							   JOSSO_SECURITY_CHECK_URI
							  );
		} else if (isPublicResource(r)) {
			requireLogin = 0;
		} else {
			location = apr_psprintf(r->pool, "%s?josso_partnerapp_host=%s&josso_partnerapp_id=%s&josso_partnerapp_ctx=%s&josso_back_to=%s%s",
							   cfg->gatewayLoginUrl,
							   host,
							   getRequester(r),
							   r->uri,
							   getRequestedUrl(r, 0),
							   JOSSO_SECURITY_CHECK_URI
							  );
		}

		if (requireLogin) {
			//set non cache headers
			prepareNonCacheResponse(r);
			apr_table_setn(r->err_headers_out, "Location", location);
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
						  "Redirecting user for authentication to [%s]", location);

			return HTTP_MOVED_TEMPORARILY;
		}
	}

	return OK;
}


/* Authorization based on the single user or its roles */

static int check_user_access(request_rec *r)
{
	register int x, i;
    const char *t, *w;
    const char *role;
    int m = r->method_number;
    const apr_array_header_t *reqs_arr = ap_requires(r);
    require_line *reqs;

    ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
				  "Authorizing SSO user [%s]", r->user);

    reqs = (require_line *)reqs_arr->elts;

    for (x = 0; x < reqs_arr->nelts; x++) {

        if (!(reqs[x].method_mask & (AP_METHOD_BIT << m))) {
            continue;
        }

        t = reqs[x].requirement;
        w = ap_getword_white(r->pool, &t);

        if (!strcasecmp(w, "role")) {
            apr_array_header_t *roles = apr_table_get(r->notes, JOSSO_ROLE_NOTE);

            while (t[0]) {
            	w = ap_getword_conf(r->pool, &t);
		        for (i = 0; i < roles->nelts; i++ ) {
		        	role = (char *) ((char **)roles->elts)[i];
					if (!strcmp(role, w)) {
						ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
									  "Role-based Authorization successful for user [%s]", r->user);
						return OK;
					}
				}
			}
		} else
		if (!strcasecmp(w, "user")) {
			while (t[0]) {
				w = ap_getword_conf(r->pool, &t);
				if (!strcmp(r->user, w)) {
					ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
								  "Authorization successful for user [%s]", r->user);
					return OK;
				}
			}
		} else
		if (!strcasecmp(w, "sso-session-or-anonymous")) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
								"Authorization successful for public resource", r->unparsed_uri);
			return OK;
		}
    }

	ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Access not granted to user [%s]", r->user);
    return DECLINED;
}

static char *resolveAssertion(request_rec *r, const char* assertionId) {
   struct soap *soap = soap_new();
   char *sessionId[1];
   char *targetSessionId = NULL;
   struct ns3__ResolveAuthenticationAssertionRequestType req;
   struct ns3__ResolveAuthenticationAssertionResponseType rsp;

   req.assertionId = assertionId;
   req.requester = getRequester(r);

   setupSSLContext(soap, r);

   if (soap_call___ns4__resolveAuthenticationAssertion(soap, getSSOIdentityProviderServiceEndpoint(r), NULL, &req, &rsp) == SOAP_OK) {
	   targetSessionId = apr_pstrdup(r->pool, rsp.ssoSessionId);
   } else {
	   //TODO: Error handling
	   ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Soap error... error code: [%d], error message: [%s]",
				   soap->error, *soap_faultdetail(soap));
   }

   soap_end(soap);

   return targetSessionId;
}


static char* findUserInSession(request_rec *r, char *sessionId) {
   struct soap *soap = soap_new();
   char user[255];
   char *targetUser = NULL;
   struct ns3__FindUserInSessionRequestType req;
   struct ns3__FindUserInSessionResponseType rsp;

   req.ssoSessionId = sessionId;
   req.requester = getRequester(r);

   setupSSLContext(soap, r);

   if (soap_call___ns5__findUserInSession(soap, getSSOIdentityManagerServiceEndpoint(r), NULL, &req, &rsp) == SOAP_OK) {
	   targetUser = apr_pstrdup(r->pool, rsp.SSOUser->name);
   } else {
	   //TODO: Error handling
	   ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Soap error... error code: [%d], error message: [%s]",
				   soap->error, *soap_faultdetail(soap));
   }

   soap_end(soap);

   return targetUser;
}

static apr_array_header_t* findRolesBySSOSessionId(request_rec *r, char *sessionId) {
   struct soap *soap = soap_new();
   apr_array_header_t *targetRoles = NULL;
   struct ns3__FindRolesBySSOSessionIdRequestType req;
   struct ns3__FindRolesBySSOSessionIdResponseType rsp;

   req.ssoSessionId = sessionId;
   req.requester = getRequester(r);

   setupSSLContext(soap, r);

   if (soap_call___ns5__findRolesBySSOSessionId(soap, getSSOIdentityManagerServiceEndpoint(r), NULL, &req, &rsp) == SOAP_OK) {
	   targetRoles = apr_array_make(r->pool, rsp.__sizeroles, sizeof(char **));

	   int i;
	   for ( i=0; i < rsp.__sizeroles; i++) {
		   char **roleRef = apr_array_push(targetRoles);
		   *roleRef = apr_pstrdup(r->pool, rsp.roles[i].name);
	   }
   } else {
	   //TODO: Error handling
	   ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Soap error... error code: [%d], error message: [%s]",
				   soap->error, *soap_faultdetail(soap));
   }

   soap_end(soap);

   return targetRoles;

}

static void accessSession(request_rec *r, char *sessionId) {
   struct soap *soap = soap_new();
   struct ns3__AccessSessionRequestType req;
   struct ns3__AccessSessionResponseType rsp;

   req.ssoSessionId = sessionId;
   req.requester = getRequester(r);

   setupSSLContext(soap, r);

   if (soap_call___ns6__accessSession(soap, getSSOSessionManagerServiceEndpoint(r), NULL, &req, &rsp) == SOAP_OK) {
   } else {
	   //TODO: Error handling
	   ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Soap error... error code: [%d], error message: [%s]",
	   			  soap->error, *soap_faultdetail(soap));
   }

   soap_end(soap);
}

static const char *getSSOIdentityManagerServiceEndpoint(request_rec *r) {
	char *identityManagerSvcEpUrl;
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);

	identityManagerSvcEpUrl = apr_psprintf (r->pool, "%s://%s:%d/%s", getGatewayEndpointScheme(r),
											cfg->gatewayEndpointHost,
											cfg->gatewayEndpointPort,
											cfg->identityManagerServicePath);

	return identityManagerSvcEpUrl;
}

static const char *getSSOSessionManagerServiceEndpoint(request_rec *r) {
	char *sessionManagerSvcEpUrl;
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);

	sessionManagerSvcEpUrl = apr_psprintf (r->pool, "%s://%s:%d/%s", getGatewayEndpointScheme(r),
										   cfg->gatewayEndpointHost,
										   cfg->gatewayEndpointPort,
										   cfg->sessionManagerServicePath);

	return sessionManagerSvcEpUrl;
}

static const char *getSSOIdentityProviderServiceEndpoint(request_rec *r) {
	const char *identityProviderSvcEpUrl;
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);

	identityProviderSvcEpUrl = apr_psprintf (r->pool, "%s://%s:%d/%s", getGatewayEndpointScheme(r),
											 cfg->gatewayEndpointHost,
											 cfg->gatewayEndpointPort,
											 cfg->identityProviderServicePath);

    return identityProviderSvcEpUrl;

}


static char *form_value(apr_pool_t *pool, apr_hash_t *form, const char *key)
{
	if (form == NULL) {
		return NULL;
	}

	apr_array_header_t *v_arr = apr_hash_get(form, key,
                               APR_HASH_KEY_STRING);

    /* Caveat: this is ambiguous because values may contain commas */
    return apr_array_pstrcat(pool, v_arr, ',');
}

static apr_hash_t *parse_form_from_GET(request_rec *r)
{
    apr_hash_t* t =  parse_form_from_string(r, r->args);
    return t;
}

/* Parse form data from a string. The input string is NOT preserved. */
static apr_hash_t *parse_form_from_string(request_rec *r, char *args)
{
    apr_hash_t *form;
    apr_array_header_t *values;
    char *pair;
    char *eq;
    const char *delim = "&";
    char *last;
    char **ptr;


	if (args == NULL) {
         return NULL;
    }

     form = apr_hash_make(r->pool);

     /* Split the input on '&' */
     for (pair = apr_strtok(args, delim, &last); pair != NULL;
        pair = apr_strtok(NULL, delim, &last)) {
         for (eq = pair; *eq; ++eq) {
              if (*eq == '+') {
                  *eq = ' ';
              }
         }
         /* split into Key / Value and unescape it */
         eq = strchr(pair, '=');
         if (eq) {
              *eq++ = '\0';
         ap_unescape_url(pair);
         ap_unescape_url(eq);
         }
         else {
              eq = "";
         ap_unescape_url(pair);
         }
          /* Store key/value pair in our form hash. Given that there
           * may be many values for the same key, we store values
           * in an array (which we'll have to create the first
           * time we encounter the key in question).
           */
         values = apr_hash_get(form, pair, APR_HASH_KEY_STRING);
         if (values == NULL) {
              values = apr_array_make(r->pool, 1, sizeof(const char*));
              apr_hash_set(form, pair, APR_HASH_KEY_STRING, values);
         }
         ptr = apr_array_push(values);
         *ptr = apr_pstrdup(r->pool, eq);
     }
     return form;
}

static char *get_cookie (request_rec * r, char *name)
{
    const char *cookie_header;
    char *chp;
    char *cookie, *ptr;
    char *name_w_eq;
    apr_pool_t *p = r->pool;
    int i;
    const int n = 0;

    /* get cookies */
    if (!(cookie_header = apr_table_get (r->headers_in, "Cookie"))) {
        return NULL;
    }

    /* add an equal on the end */
    name_w_eq = apr_pstrcat (p, name, "=", NULL);

    for (chp=(char*)cookie_header,i=0;i<=n;i++) {
      if (!(chp = strstr(chp, name_w_eq))) return NULL;
       chp += strlen (name_w_eq);
    }

    cookie = apr_pstrdup (p, chp);
    ptr = cookie;
    while (*ptr) {
        if (*ptr == ';')
            *ptr = 0;
        ptr++;
    }

    if (*cookie) {
        return cookie;
    }
    return (NULL);
}

static char *set_session_cookie (request_rec * r, char *name, char *value, char *path, char *domain)
{
    apr_pool_t *p = r->pool;
	char* new_session_cookie;
    const char* cookie_type = apr_psprintf (p, "%s", name);
    const char* transport = apr_psprintf (p, "%s", ap_http_scheme(r));

    if (domain != NULL)
    	new_session_cookie = apr_psprintf (p, "%s=%s; path=%s;%s; HttpOnly", name, value, path, domain);
    else
    	new_session_cookie = apr_psprintf (p, "%s=%s; path=%s; HttpOnly", name, value, path);

    ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "name: [%s],cookie_type: [%s],ct_result: [%d], https?: [%s]",name,cookie_type,strcmp(cookie_type , JOSSO_SINGLE_SIGN_ON_COOKIE),transport);

    if (!strcmp(transport,"https") & !strcmp(cookie_type , JOSSO_SINGLE_SIGN_ON_COOKIE))
	    new_session_cookie = apr_psprintf (p, "%s ; Secure", new_session_cookie);

    apr_table_add (r->err_headers_out, "Set-Cookie", new_session_cookie);
    apr_table_add (r->headers_out, "Set-Cookie", new_session_cookie);

}


static char * generate_uuid(request_rec * r, int length) {
	char *value = NULL;
	int i;
	float x;

	value = apr_palloc(r->pool, (length + 1));
	srand((unsigned int)time(0) * r->connection->id);
	for (i = 0; i < length; i++) {
		x = (float) rand()/ RAND_MAX;
		ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "rand [%f]", x);
		value[i] = (char) 65 + (x * 26.0);
	}
	value[length] = '\0';
	ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "uuid [%s]", value);

	return value;
}

static void createPHP5SecurityContext(request_rec * r) {
    apr_table_t *e;

    /* use a temporary apr_table_t which we'll overlap onto
     * r->subprocess_env later
     * (exception: if r->subprocess_env is empty at the start,
     * write directly into it)
     */
    if (apr_is_empty_table(r->subprocess_env)) {
        e = r->subprocess_env;
    }
    else {
        e = apr_table_make(r->pool, 3);
    }

    /* the user for the SSO session is mapped to the PHP security context user and
     * the SSO session token to the PHP security context password.
     */
    apr_table_addn(e, "PHP_AUTH_USER", r->user);
    apr_table_addn(e, "PHP_AUTH_PW", get_cookie(r, JOSSO_SINGLE_SIGN_ON_COOKIE));
    apr_table_addn(e, "PHP_AUTH_TYPE", "JOSSO");

    if (e != r->subprocess_env) {
      apr_table_overlap(r->subprocess_env, e, APR_OVERLAP_TABLES_SET);
    }

}

static char *remove_session_cookie(request_rec *r, char *name, char *path, char *domain)
{
    apr_pool_t *p = r->pool;
	const char* new_session_cookie;

	if (domain != NULL)
    	new_session_cookie = apr_psprintf (p, "%s=-; max-age=0; expires=0; path=%s;%s", name, path, domain);
    else
    	new_session_cookie = apr_psprintf (p, "%s=-; max-age=0; expires=0; path=%s", name, path);

    apr_table_add(r->err_headers_out, "Set-Cookie", new_session_cookie);
    apr_table_add(r->headers_out, "Set-Cookie", new_session_cookie);
}

/*
 *      1) Required     - The LoginModule is required to succeed.
 *			If it succeeds or fails, authentication still continues
 *			to proceed down the LoginModule list.
 *
 *      3) Sufficient   - The LoginModule is not required to
 *			succeed.  If it does succeed, control immediately
 *			returns to the application (authentication does not
 *			proceed down the LoginModule list).
 *			If it fails, authentication continues down the
 *			LoginModule list.
 *
 *      3) Optional     - The LoginModule is not required to
 *			succeed.  If it succeeds or fails,
 *			authentication still continues to proceed down the
 *			LoginModule list.
*/
static int isAutomaticLoginRequired(request_rec *r)
{
	// If any required module returns false, this will be false
	int requiredFlag = -1;

	// If any sufficient module returns true, this will be true
	int sufficientFlag = -1;

	if (automaticStrategiesSize == 0) {
		automaticStrategies = malloc(sizeof *automaticStrategies);
		base_auto_login_rec default_auto_login;
		default_auto_login.strategy = JOSSO_DEFAULT_AUTH_LOGIN_STRATEGY;
		default_auto_login.mode = JOSSO_AUTH_LOGIN_SUFFICIENT;
		automaticStrategies[automaticStrategiesSize] = &default_auto_login;
		automaticStrategiesSize++;
    }

	int i;
	for (i=0; i<automaticStrategiesSize; i++) {
		base_auto_login_rec *autoLoginStrategy = automaticStrategies[i];

        if (!strcmp(autoLoginStrategy->mode, JOSSO_AUTH_LOGIN_SUFFICIENT)) {
			if (isAutoLoginRequired(i, r)) {
				sufficientFlag = 1;
				break; // Stop evaluation
			}
        }

		if (!strcmp(autoLoginStrategy->mode, JOSSO_AUTH_LOGIN_REQUIRED)) {
			if (!isAutoLoginRequired(i, r)) {
				requiredFlag = 0;
			} else if (requiredFlag == -1) {
				requiredFlag = 1;
			}
        }

		// This does not affect the outcome of the evaluation
		if (!strcmp(autoLoginStrategy->mode, JOSSO_AUTH_LOGIN_OPTIONAL)) {
			isAutoLoginRequired(i, r);
        }
	}

	// If any required module returned a value, use it.
	if (requiredFlag != -1) {
		return requiredFlag;
	}

	// If any sufficient modules returned a value, use it; otherwise return false.
	return sufficientFlag != -1 && sufficientFlag;
}

static int isAutoLoginRequired(int strategyIndex, request_rec *r)
{
	base_auto_login_rec *autoLoginStrategy = automaticStrategies[strategyIndex];
	if (!strcmp(autoLoginStrategy->strategy, JOSSO_DEFAULT_AUTH_LOGIN_STRATEGY)) {
		isDefaultAutoLoginRequired(autoLoginStrategy, r);
	} else if (!strcmp(autoLoginStrategy->strategy, JOSSO_URLBASED_AUTH_LOGIN_STRATEGY)) {
		isUrlBasedAutoLoginRequired(automaticStrategies[strategyIndex], r);
	} else if (!strcmp(autoLoginStrategy->strategy, JOSSO_BOT_AUTH_LOGIN_STRATEGY)) {
		isBotAutoLoginRequired(automaticStrategies[strategyIndex], r);
	}
}

static int isDefaultAutoLoginRequired(base_auto_login_rec *default_auto_login, request_rec *r)
{
    const char* autoLoginExecuted = get_cookie(r, JOSSO_AUTOMATIC_LOGIN_EXECUTED);
    const char* path = getContextPath(r);

    if (autoLoginExecuted == NULL || !strcmp(autoLoginExecuted,"0")) {
		ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Automatic login was never executed. Require Autologin!");
		set_session_cookie(r, JOSSO_AUTOMATIC_LOGIN_EXECUTED, "1", path, NULL);
		return 1;
    }

	const char* referer = apr_table_get(r->headers_in, "referer");
	/* If we have a referer host that differs from our we require an autologinSSs */
	if (referer != NULL && strcmp(referer, "")) {
		const char* oldReferer = get_cookie(r, JOSSO_AUTOMATIC_LOGIN_REFERER);
		if (oldReferer != NULL && !strcasecmp(referer, oldReferer)) {
        	ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Referer already processed [%s]", referer);
            /* cleanup so we give this referer a chance in the future! */
			remove_session_cookie(r, JOSSO_AUTOMATIC_LOGIN_REFERER, path, NULL);
            return 0;
        }

		const char *baseUrl = createBaseUrl(r);

		ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Processing referer [%s] for host [%s]", referer, baseUrl);

		if (strncmp(referer, baseUrl, strlen(baseUrl)) != 0) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Referer found differs from current host.  Require Autologin!");

			/* Store referer for future reference! */
			set_session_cookie(r, JOSSO_AUTOMATIC_LOGIN_REFERER, referer, path, NULL);
			return 1;
        }
    }

    return 0;
}

/*
 * This strategy returns false if the accessed URL matches any of the configured URL patterns.
 * The patterns are regular expressions.
 */
static int isUrlBasedAutoLoginRequired(urlbased_auto_login_rec *urlbased_auto_login, request_rec *r)
{
	int autoLoginRequired = 1;
	if (urlbased_auto_login->url_patterns_size > 0) {
		const char *requestedUrl = getRequestedUrl(r, 1);
		int i;
		for (i=0; i<urlbased_auto_login->url_patterns_size; i++) {
			ap_regex_t *regexp = NULL;
			regexp = ap_pregcomp(r->pool, urlbased_auto_login->url_patterns[i], REG_NOSUB);
			if (regexp != NULL) {
				ap_regmatch_t regm[10];
				if (!ap_regexec(regexp, requestedUrl, 0, regm, 0)) {
					ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Autologin is not required! Ignored url pattern: %s",
								  urlbased_auto_login->url_patterns[i]);
					autoLoginRequired = 0;
					break;
				}
			} else {
				ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Regular expression could not be compiled.");
			}
		}
	}
	return autoLoginRequired;
}

/*
 * This will not require an automatic login when a bot is crawling the site.
 */
static int isBotAutoLoginRequired(bot_auto_login_rec *bot_auto_login, request_rec *r)
{
	const char *user_agent = apr_table_get(r->headers_in, "User-Agent");

	robot *robot = apr_hash_get(bot_auto_login->robots, user_agent, APR_HASH_KEY_STRING);
	if (robot != NULL) {
		ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "Autologin not required for bot: %s", user_agent);
		return 0;
	}

	return 1;
}

static const char *getHost(request_rec *r)
{
    const char *host;
    host = r->hostname;

    if (host == NULL) {
	host = ap_get_server_name(r);
    }
    return host;
}

static const char *createBaseUrl(request_rec *r)
{
	const char *host = getHost(r);

	int port = r->connection->local_addr->port;

	const char *baseUrl;

	if (port == 80 || port == 443) {
		baseUrl = apr_psprintf(r->pool, "%s://%s",
				ap_http_scheme(r),
				host
		);
	} else {
		baseUrl = apr_psprintf(r->pool, "%s://%s:%d",
				ap_http_scheme(r),
				host,
				port
		);
	}

	return baseUrl;
}

static const char *getRequestedUrl(request_rec *r, int includeParameters)
{
	const char *host;
	host = getHost(r);

	const char *uri;
	if (!includeParameters) {
		uri = r->uri;
	} else {
		uri = r->unparsed_uri;
	}

	//TODO: convey protocol and query string
	const char *requestedUrl;
	requestedUrl = apr_psprintf(r->pool, "%s://%s:%d%s",
			ap_http_scheme(r),
			host,
			r->connection->local_addr->port,
			uri
	);

	return requestedUrl;
}

static const char *getContextPath(request_rec *r)
{
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);

	const char *path = cfg->context;
	if (path == NULL) {
		path = "/";
	}

	return path;
}

static int isPublicResource(request_rec *r)
{
	register int x;
    const char *t, *w;
    int m = r->method_number;
    const apr_array_header_t *reqs_arr = ap_requires(r);
    require_line *reqs;

    reqs = (require_line *)reqs_arr->elts;

    for (x = 0; x < reqs_arr->nelts; x++) {

        if (!(reqs[x].method_mask & (AP_METHOD_BIT << m))) {
            continue;
        }

        t = reqs[x].requirement;
        w = ap_getword_white(r->pool, &t);

		if (!strcasecmp(w, "sso-session-or-anonymous")) {
			return 1;
		}
    }

    return 0;
}

static void prepareNonCacheResponse(request_rec *r)
{
	apr_table_setn(r->err_headers_out, "Cache-Control", "no-cache, no-store");
    apr_table_setn(r->headers_out, "Cache-Control", "no-cache, no-store");

	apr_table_setn(r->err_headers_out, "Pragma", "no-cache");
    apr_table_setn(r->headers_out, "Pragma", "no-cache");

	apr_table_setn(r->err_headers_out, "Expires", "0");
    apr_table_setn(r->headers_out, "Expires", "0");
}

static const char *getGatewayEndpointScheme(request_rec *r)
{
	const char *scheme = "http";
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);
	if (cfg->GatewayEndpointSSLEnable) {
		scheme = "https";
	}
	return scheme;
}

static char *appendChar(char *value, char ch) {
	if (value == "" && (ch == ' ' || ch == '\t')) {
        return value;
    }
	int len = strlen(value);
    char *ret = (char*)malloc(len * sizeof(char) + 2);
    strcpy(ret, value);
    ret[len] = ch;
    ret[len+1] = '\0';
    return ret;
}

/*
 * Load robots from the file.
 */
static int loadRobots(bot_auto_login_rec *bot_auto_login)
{
	apr_status_t rv;
    apr_pool_t *mp;
    apr_file_t *f;

    apr_pool_create(&mp, NULL);

	if ((rv = apr_file_open(&f, bot_auto_login->bots_file, APR_FOPEN_READ, APR_OS_DEFAULT, mp)) == APR_SUCCESS) {
		bot_auto_login->robots = apr_hash_make(mp);

		robot *r = apr_pcalloc(mp, sizeof(robot));

		char ch;
		char *name = "";
		char *value = "";
		char *previous_name = name;
		int name_found = 0;

		ap_regex_t *robot_regexp = NULL;
		ap_regex_t *modified_regexp = NULL;
		char *robot_pattern = "robot-*";
		char *modified_pattern = "modified-*";
		robot_regexp = ap_pregcomp(mp, robot_pattern, REG_NOSUB);
		modified_regexp = ap_pregcomp(mp, modified_pattern, REG_NOSUB);
		ap_regmatch_t regm[10];

		while (apr_file_getc(&ch, f) == APR_SUCCESS) {
			if (ch != '\n') {
				if (ch != ':') {
					if (!name_found) {
						name = appendChar(name, ch);
					} else {
						value = appendChar(value, ch);
					}
				} else {
					if (!name_found && (!ap_regexec(robot_regexp, name, 0, regm, 0) ||
							!ap_regexec(modified_regexp, name, 0, regm, 0))) {
						name_found = 1;
					} else {
						value = appendChar(value, ch);
					}
				}
			} else {
				if (!name_found) {
					value = strcat(name, value);
					name = "";
				}
				char new_name[50];
				char new_value[300];

				if (!strcmp(name, "") && !strcmp(value, "")) {
					if (r->user_agent != NULL && r->user_agent != "") {
						apr_hash_set(bot_auto_login->robots, r->user_agent, APR_HASH_KEY_STRING, r);
					}
					r = apr_pcalloc(mp, sizeof(robot));
					previous_name = "";
				} else {
					if (!name_found) {
						name = previous_name;
					}
					setRobotProperty(r, name, value, !name_found, mp);
					previous_name = name;
				}

				name = "";
				value = "";
				name_found = 0;
			}
		}

		apr_file_close(f);
	}

	return 0;
}

/*
 * Sets robot property value.
 *
 * @param robot robot
 * @param name property name
 * @param value property value
 * @param append true if value should be appended to existing value, false otherwise
 * @param mp memory pool
 */
static int setRobotProperty(robot *robot, char *name, char *value, int append, apr_pool_t *mp)
{
	if (robot == NULL || !strcmp(name, "")) {
		return -1;
	}

	if (!robot_regexps_compiled) {
		compileRobotRegexp(mp);
	}

	ap_regmatch_t regm[10];

	//apr_collapse_spaces(value, value);

	if (!ap_regexec(robot_regexps[0], name, 0, regm, 0)) {
		robot->id = value;
	} else if (!ap_regexec(robot_regexps[1], name, 0, regm, 0)) {
		robot->name = value;
	} else if (!ap_regexec(robot_regexps[2], name, 0, regm, 0)) {
		robot->cover_url = value;
	} else if (!ap_regexec(robot_regexps[3], name, 0, regm, 0)) {
		robot->details_url = value;
	} else if (!ap_regexec(robot_regexps[4], name, 0, regm, 0)) {
		robot->owner_name = value;
	} else if (!ap_regexec(robot_regexps[5], name, 0, regm, 0)) {
		robot->owner_url = value;
	} else if (!ap_regexec(robot_regexps[6], name, 0, regm, 0)) {
		robot->owner_email = value;
	} else if (!ap_regexec(robot_regexps[7], name, 0, regm, 0)) {
		robot->status = value;
	} else if (!ap_regexec(robot_regexps[8], name, 0, regm, 0)) {
		robot->purpose = value;
	} else if (!ap_regexec(robot_regexps[9], name, 0, regm, 0)) {
		robot->type = value;
	} else if (!ap_regexec(robot_regexps[10], name, 0, regm, 0)) {
		robot->platform = value;
	} else if (!ap_regexec(robot_regexps[11], name, 0, regm, 0)) {
		robot->availability = value;
	} else if (!ap_regexec(robot_regexps[12], name, 0, regm, 0)) {
		robot->exclusion_useragent = value;
	} else if (!ap_regexec(robot_regexps[13], name, 0, regm, 0)) {
		robot->exclusion = value;
	} else if (!ap_regexec(robot_regexps[14], name, 0, regm, 0)) {
		robot->noindex = value;
	} else if (!ap_regexec(robot_regexps[15], name, 0, regm, 0)) {
		robot->host = value;
	} else if (!ap_regexec(robot_regexps[16], name, 0, regm, 0)) {
		robot->from = value;
	} else if (!ap_regexec(robot_regexps[17], name, 0, regm, 0)) {
		robot->user_agent = value;
	} else if (!ap_regexec(robot_regexps[18], name, 0, regm, 0)) {
		robot->language = value;
	} else if (!ap_regexec(robot_regexps[19], name, 0, regm, 0)) {
		if (append && robot->description != NULL && robot->description != "") {
			strcat(robot->description, " ");
			strcat(robot->description, value);
		} else {
			robot->description = value;
		}
	} else if (!ap_regexec(robot_regexps[20], name, 0, regm, 0)) {
		if (append && robot->history != NULL && robot->history != "") {
			strcat(robot->history, " ");
			strcat(robot->history, value);
		} else {
			robot->history = value;
		}
	} else if (!ap_regexec(robot_regexps[21], name, 0, regm, 0)) {
		robot->environment = value;
	} else if (!ap_regexec(robot_regexps[22], name, 0, regm, 0)) {
		robot->modified_date = value;
	} else if (!ap_regexec(robot_regexps[23], name, 0, regm, 0)) {
		robot->modified_by = value;
	}

	return 0;
}

/*
 * Initialize/compile robot regular expressions.
 */
static void compileRobotRegexp(apr_pool_t *mp) {
	char *id_pattern = "robot-id*";
	robot_regexps[0] = ap_pregcomp(mp, id_pattern, REG_NOSUB);

	char *name_pattern = "robot-name*";
	robot_regexps[1] = ap_pregcomp(mp, name_pattern, REG_NOSUB);

	char *cover_url_pattern = "robot-cover-url*";
	robot_regexps[2] = ap_pregcomp(mp, cover_url_pattern, REG_NOSUB);

	char *details_url_pattern = "robot-details-url*";
	robot_regexps[3] = ap_pregcomp(mp, details_url_pattern, REG_NOSUB);

	char *owner_name_pattern = "robot-owner-name*";
	robot_regexps[4] = ap_pregcomp(mp, owner_name_pattern, REG_NOSUB);

	char *owner_url_pattern = "robot-owner-url*";
	robot_regexps[5] = ap_pregcomp(mp, owner_url_pattern, REG_NOSUB);

	char *owner_email_pattern = "robot-owner-email*";
	robot_regexps[6] = ap_pregcomp(mp, owner_email_pattern, REG_NOSUB);

	char *status_pattern = "robot-status*";
	robot_regexps[7] = ap_pregcomp(mp, status_pattern, REG_NOSUB);

	char *purpose_pattern = "robot-purpose*";
	robot_regexps[8] = ap_pregcomp(mp, purpose_pattern, REG_NOSUB);

	char *type_pattern = "robot-type*";
	robot_regexps[9] = ap_pregcomp(mp, type_pattern, REG_NOSUB);

	char *platform_pattern = "robot-platform*";
	robot_regexps[10] = ap_pregcomp(mp, platform_pattern, REG_NOSUB);

	char *availability_pattern = "robot-availability*";
	robot_regexps[11] = ap_pregcomp(mp, availability_pattern, REG_NOSUB);

	char *exclusion_useragent_pattern = "robot-exclusion-useragent*";
	robot_regexps[12] = ap_pregcomp(mp, exclusion_useragent_pattern, REG_NOSUB);

	char *exclusion_pattern = "robot-exclusion*";
	robot_regexps[13] = ap_pregcomp(mp, exclusion_pattern, REG_NOSUB);

	char *noindex_pattern = "robot-noindex*";
	robot_regexps[14] = ap_pregcomp(mp, noindex_pattern, REG_NOSUB);

	char *host_pattern = "robot-host*";
	robot_regexps[15] = ap_pregcomp(mp, host_pattern, REG_NOSUB);

	char *from_pattern = "robot-from*";
	robot_regexps[16] = ap_pregcomp(mp, from_pattern, REG_NOSUB);

	char *useragent_pattern = "robot-useragent*";
	robot_regexps[17] = ap_pregcomp(mp, useragent_pattern, REG_NOSUB);

	char *language_pattern = "robot-language*";
	robot_regexps[18] = ap_pregcomp(mp, language_pattern, REG_NOSUB);

	char *description_pattern = "robot-description*";
	robot_regexps[19] = ap_pregcomp(mp, description_pattern, REG_NOSUB);

	char *history_pattern = "robot-history*";
	robot_regexps[20] = ap_pregcomp(mp, history_pattern, REG_NOSUB);

	char *environment_pattern = "robot-environment*";
	robot_regexps[21] = ap_pregcomp(mp, environment_pattern, REG_NOSUB);

	char *modified_date_pattern = "modified-date*";
	robot_regexps[22] = ap_pregcomp(mp, modified_date_pattern, REG_NOSUB);

	char *modified_by_pattern = "modified-by*";
	robot_regexps[23] = ap_pregcomp(mp, modified_by_pattern, REG_NOSUB);

	robot_regexps_compiled = 1;
}

static const char *getRequester(request_rec *r)
{
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);
	return cfg->partnerAppId;
}

static int setupSSLContext(struct soap *soap, request_rec *r)
{
	int result = 0;
	auth_josso_config_rec *cfg = ap_get_module_config(r->per_dir_config, &auth_josso_module);

#ifdef WITH_OPENSSL
	if (cfg->GatewayEndpointSSLEnable) {
		unsigned short flags = SOAP_SSL_NO_AUTHENTICATION;
		if (cfg->EnableGatewayAuthentication) {
			flags = SOAP_SSL_DEFAULT | SOAP_SSL_SKIP_HOST_CHECK;
		}
		soap_ssl_init();
		result = soap_ssl_client_context(soap,
				 flags,
				 NULL, 	/* keyfile: required only when client must authenticate to server */
				 NULL, 	/* password to read the key file (not used with GNUTLS) */ 
				 cfg->sslServerCertFile, 	/* cacert file to store trusted certificates (needed to verify server) */
				 cfg->sslServerCertDir,	/* if randfile!=NULL: use a file with random data to seed randomness */ 
				 NULL //cfg->sslRandFile
			);
		if (result != SOAP_OK) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
					"Error setting up soap SSL client context... "
					"error code: [%d], error message: [%s]",
					soap->error, *soap_faultdetail(soap));
		}
	}
#endif
	return result;
}

static void register_hooks(apr_pool_t *p)
{
	ap_hook_auth_checker(check_user_access, NULL, NULL, APR_HOOK_MIDDLE);
	ap_hook_check_user_id(authenticate_josso_user,NULL,NULL,APR_HOOK_MIDDLE);
}

module AP_MODULE_DECLARE_DATA auth_josso_module =
{
    STANDARD20_MODULE_STUFF,
    create_auth_josso_dir_config,  /* dir config creater */
    NULL,                          /* dir merger --- default is to override */
    NULL,  						   /* create per-server config structure */
    NULL,                          /* merge server config */
    auth_josso_cmds,               /* command apr_table_t */
    register_hooks                 /* register hooks */
};
