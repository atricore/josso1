/***************************************************************************
 * Description: ISAPI plugin for IIS/PWS                                   *
 * Author:      Sebastian Gonzalez Oyuela <sgonzalez@josso.org>            *
 * Version:     $Revision: 0000 $                                          *
 ***************************************************************************/

// This define is needed to include wincrypt,h, needed to get client certificates
#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0400
#endif

/*
#define _DEBUG 1
#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <iostream>
#include <crtdbg.h>
*/

/*
#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>
*/
#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>

//#include <strsafe.h>

#include <JOSSOIsapiAgent/isapi/JOSSOIsapiPlugin.hpp>

#include <JOSSOIsapiAgent/isapi/IsapiSSOAgent.hpp>
#include <JOSSOIsapiAgent/isapi/FilterAgentRequest.hpp>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

// Static Agent reference, should not be reinitialized on each request.
static IsapiSSOAgent *ssoAgent = new IsapiSSOAgent();


// This is used as a mutex object in multi threading environments.
CRITICAL_SECTION g_AgentLock;

DWORD
OnPreprocHeaders(
    HTTP_FILTER_CONTEXT *           pfc,
    HTTP_FILTER_PREPROC_HEADERS *   pPPH
);



//--------------------------------------------------------------------------------
// JOSSO ISAPI Filter entry points
//--------------------------------------------------------------------------------
BOOL WINAPI GetFilterVersion (HTTP_FILTER_VERSION* pVer)
{

    BOOL rv = TRUE;
    ULONG http_filter_revision = HTTP_FILTER_REVISION;

	syslog(JK_LOG_DEBUG_LEVEL, "Providing ISAPI Filter version");

    pVer->dwFilterVersion = pVer->dwServerFilterVersion;

    if (pVer->dwFilterVersion > http_filter_revision) {
        pVer->dwFilterVersion = http_filter_revision;
    }
    
	// TODO : Are this the right events ?

    pVer->dwFlags = SF_NOTIFY_ORDER_HIGH |
					SF_NOTIFY_PREPROC_HEADERS;

    StringCbCopy(pVer->lpszFilterDesc, SF_MAX_FILTER_DESC_LEN, (VERSION_STRING));
    return rv;

}  /* GetFilterVersion */

DWORD WINAPI HttpFilterProc (HTTP_FILTER_CONTEXT* pfc,
                             DWORD dwNotificationType, VOID* pvNotification)
{

    switch ( dwNotificationType )
    {
    case SF_NOTIFY_PREPROC_HEADERS:
        return OnPreprocHeaders(
            pfc,
            (HTTP_FILTER_PREPROC_HEADERS *) pvNotification );
    }

    return SF_STATUS_REQ_NEXT_NOTIFICATION;
}

DWORD OnPreprocHeaders( HTTP_FILTER_CONTEXT *           pfc,
						HTTP_FILTER_PREPROC_HEADERS *   pPPH)
{

	int rc = SF_STATUS_REQ_NEXT_NOTIFICATION; 

	InitializeCriticalSection( &g_AgentLock );
	if (!ssoAgent->isStarted()) {
		ssoAgent->start();
	}
	LeaveCriticalSection(&g_AgentLock);
	

	if (!ssoAgent->isStarted()) {
		syslog(JK_LOG_ERROR_LEVEL, "JOSSO Agent is not running ...");
		return SF_STATUS_REQ_ERROR;
	}

	// Create request / response objects
	SSOAgentRequest *req = ssoAgent->initIsapiFilterRequest(pfc, SF_NOTIFY_PREPROC_HEADERS, pPPH);
	if (req == NULL) {
		jk_log(ssoAgent->logger, JK_LOG_ERROR, "Cannot initialize SSO Agent Request");
		syslog(JK_LOG_ERROR_LEVEL, "Cannot initialize SSO Agent Request");
		return SF_STATUS_REQ_ERROR;
	}

	SSOAgentResponse *res = ssoAgent->initIsapiFilterResponse(pfc, SF_NOTIFY_PREPROC_HEADERS, pPPH);
	if (res == NULL) {
		jk_log(ssoAgent->logger, JK_LOG_ERROR, "Cannot initialize SSO Agent Response");
		syslog(JK_LOG_ERROR_LEVEL, "Cannot initialize SSO Agent Response");
		return SF_STATUS_REQ_ERROR;
	}

	jk_log(ssoAgent->logger, JK_LOG_TRACE, "Request / Response initialized ...");

	string &path = req->getPath();
	jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Processing request for URI %s", path.c_str());

	// Verify that this URI is a partner application
	PartnerAppConfig *appCfg = ssoAgent->getPartnerAppConfig(path);
	if (appCfg == NULL) {
		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "[%s] is not associated to a partner application, ignoring", path.c_str());

	} else {

		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "[%s] is associated to %s partner application", path.c_str(), appCfg->getId());

		if (!ssoAgent->isIgnored(appCfg, req)) {

			jk_log(ssoAgent->logger, JK_LOG_TRACE, "[%s] is associated to %s partner application and will be processed", path.c_str(), appCfg->getId());

			// We found the application config, check if the resource must be ignored

			// Send P3P Header (TODO : Make it optional)
			res->addHeader("P3P", "CP=\"CAO PSA OUR\"");

			// Create security context
			if (!ssoAgent->createSecurityContext(req)) {
				// Clean up SSO Cookie

				jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Cleaning SSO Cookie");

				res->setCookie("JOSSO_SESSIONID", "-", "/");
				
			}

			bool isAuthenticated = req->isAuthenticated();
			bool isAuthorized = ssoAgent->isAuthorized(req);

			// Check for automatic Login
			if (!isAuthenticated) {

				// Only trigger automatic login if resource is public, otherwise a full login will be triggered later.
				if (isAuthorized) {

					jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Request is authorized, but not authenticated, check automatic login [%s]", path.c_str());

					if (ssoAgent->isAutomaticLoginRequired(req, res)) {
						jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Automatic login started[%s]", path.c_str());
						ssoAgent->requestLogin(req, res, appCfg, true);
						rc = SF_STATUS_REQ_FINISHED;
					}
				}

			} else {
				// This is an authenticated request, clean up any autologin state if present.
				string autoLoginExecuted = req->getCookie("JOSSO_AUTOMATIC_LOGIN_EXECUTED");
				if (!autoLoginExecuted.empty() && autoLoginExecuted.compare("-") != 0) {
					res->setCookie("JOSSO_AUTOMATIC_LOGIN_EXECUTED", "-", "/");
				}

				string autoLoginReferer = req->getCookie("JOSSO_AUTOLOGIN_REFERER");
				if (!autoLoginReferer.empty() && autoLoginReferer.compare("-") != 0) {
					res->setCookie("JOSSO_AUTOLOGIN_REFERER", "-", "/"); 
				}

			}


			// Check for security constraints
			if (!isAuthorized) {

				if (req->getRemoteUser().empty()) {
					// User is not authorized to access this resource, but was not authenticated yet -> ask for login
					jk_log(ssoAgent->logger, JK_LOG_DEBUG, "annonymous accesss to [%s] requires authentication, redirecting to login page", path.c_str());

					ssoAgent->requestLogin(req, res, appCfg, false);
					rc = SF_STATUS_REQ_FINISHED;

				} else {
					// User is not authorized to access this, but was authenticated, -> return HTTP 403 status.
					jk_log(ssoAgent->logger, JK_LOG_DEBUG, "[%s] user cannot access [%s], returning HTTP 403 status", req->getRemoteUser().c_str(), path.c_str());

					res->sendStatus(HTTP_STATUS_FORBIDDEN, "Forbidden");

					// Send a HTTP STATUS 403, Forbidden!
					rc = SF_STATUS_REQ_FINISHED;
				}
			}

		} else {
			// Configured as ignored!
			jk_log(ssoAgent->logger, JK_LOG_TRACE, "[%s] is associated to %s partner application, but ignored", path.c_str(), appCfg->getId());
		}
	}

	if (rc == SF_STATUS_REQ_NEXT_NOTIFICATION) {
		// We did not handled this request, but some headers might have been set, so flush them!
		if (!res->flushHeaders()) {
			jk_log(ssoAgent->logger, JK_LOG_ERROR, "Cannot flush headers!");
		}
	}

	// -----------------------------------------------
	// !!! IMPORTAT : CLEAN UP AFTER EACH REQUEST !!!!
	// -----------------------------------------------
	jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Processed request for URI %s", path.c_str());
	delete req;
	delete res;

	_CrtDumpMemoryLeaks();
	return rc;

} /* HttpFilterProc */

BOOL WINAPI TerminateFilter (DWORD dwFlags) 
{
	/* Called When Filter is Terminated */
	if (ssoAgent->isStarted()) {
		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "JOSSO Isapi Filter termination");
	}
	

	return TRUE;

}  /* TerminateFilter */

//--------------------------------------------------------------------------------
// JOSSO ISAPI Extension entry points
//--------------------------------------------------------------------------------
BOOL WINAPI GetExtensionVersion(HSE_VERSION_INFO * pVer)
{
	syslog(JK_LOG_DEBUG_LEVEL, "Providing ISAPI Extension version");

    pVer->dwExtensionVersion = MAKELONG(HSE_VERSION_MINOR, HSE_VERSION_MAJOR);

	StringCbCopy(pVer->lpszExtensionDesc, HSE_MAX_EXT_DLL_NAME_LEN, (VERSION_STRING));	

    return TRUE;
} /* GetExtensionVersion */

DWORD WINAPI HttpExtensionProc(LPEXTENSION_CONTROL_BLOCK lpEcb)
{
	DWORD rv = HSE_STATUS_SUCCESS;


	// Multi thread lock support until the agent is up
	// EnterCriticalSection(&CriticalSection);
	if (!ssoAgent->isStarted()) {
		syslog(JK_LOG_DEBUG_LEVEL, "HttpExtensionProc: starting SSO Agent");
		//VLDEnable();
		ssoAgent->start();
	}
	// LeaveCriticalSection(&CriticalSection);

	if (!ssoAgent->isStarted()) {
		syslog(JK_LOG_ERROR_LEVEL, "JOSSO Agent is not running ...");
		// TODO : Write error page, maybe from response ?
		return HSE_STATUS_ERROR;
	}

	// Create request / response objects
	SSOAgentRequest *req = ssoAgent->initIsapiExtensionRequest(lpEcb);
	if (req == NULL) {
		jk_log(ssoAgent->logger, JK_LOG_ERROR, "Cannot initialize SSO Agent Request");
		syslog(JK_LOG_ERROR_LEVEL, "Cannot initialize SSO Agent Request");
		// TODO : Write error page, maybe from response ?
		rv = HSE_STATUS_ERROR;
	}
	SSOAgentResponse *res = ssoAgent->initIsapiExtensionResponse(lpEcb);
	if (res == NULL) {
		jk_log(ssoAgent->logger, JK_LOG_ERROR, "Cannot initialize SSO Agent Response");
		syslog(JK_LOG_ERROR_LEVEL, "Cannot initialize SSO Agent Response");
		// TODO : Write error page, maybe from response ?
		rv = HSE_STATUS_ERROR;
	}

	if (rv == HSE_STATUS_SUCCESS) {

		res->addHeader("P3P", "CP=\"CAO PSA OUR\"");

		jk_log(ssoAgent->logger, JK_LOG_TRACE, "Request / Response initialized ...");
		string &path = req->getPath();

		// check for 'josso_login' && 'josso_login_optional'

		string pJossoLogin = req->getParameter("josso_login");
		string pJossoLoginOptional = req->getParameter("josso_login_optional");
		if (!pJossoLogin.empty() || !pJossoLoginOptional.empty()) { // Parameter is present without value, see : SSOAgentRequest::EMPTY_PARAM

			jk_log(ssoAgent->logger, JK_LOG_DEBUG, "'josso_login' || 'josso_login_optional' Received, optional %d", !pJossoLoginOptional.empty());
			
			string backTo = req->getParameter("back_to");
			if (!backTo.empty()) {
				string encodedPath = StringUtil::encode64(backTo);

				jk_log(ssoAgent->logger, JK_LOG_TRACE, "Back To PATH (encoded) %s", encodedPath.c_str());
				res->setCookie("JOSSO_RESOURCE", encodedPath, "/");
			}

			jk_log(ssoAgent->logger, JK_LOG_DEBUG, "'josso_login' received, redirecting to %s", ssoAgent->getGwyLoginUrl());
			string gwyLoginUrl (ssoAgent->buildGwyLoginUrl(req));

			if (!pJossoLoginOptional.empty()) {
				gwyLoginUrl.append("&josso_cmd=login_optional");
			}

			// Since the URL is for JOSSO Extension, we need the app. id as parameter
			string pJossoAppId = req->getParameter("josso_partnerapp_id");
			if (!pJossoAppId.empty()) {
				jk_log(ssoAgent->logger, JK_LOG_TRACE, "Partner Application ID %s", pJossoAppId.c_str());
				gwyLoginUrl.append("&josso_partnerapp_id=");
				gwyLoginUrl.append(pJossoAppId.c_str());
			}

			if (!res->sendRedirect(gwyLoginUrl.c_str()))
				rv = HSE_STATUS_ERROR;

		}

		// check for 'josso_logout'
		string pJossoLogout = req->getParameter("josso_logout");
		if (!pJossoLogout.empty()) {
			
			jk_log(ssoAgent->logger, JK_LOG_DEBUG, "'josso_logout' received");

			string gwyLogoutUrl (ssoAgent->buildGwyLogoutUrl(req));

			// Since the URL is for JOSSO Extension, we need the app. id as parameter
			string pJossoAppId = req->getParameter("josso_partnerapp_id");
			if (!pJossoAppId.empty()) {
				jk_log(ssoAgent->logger, JK_LOG_TRACE, "Partner Application ID %s", pJossoAppId.c_str());
				gwyLogoutUrl.append("&josso_partnerapp_id=");
				gwyLogoutUrl.append(pJossoAppId.c_str());
			}

			if (!res->sendRedirect(gwyLogoutUrl.c_str()))
				rv = HSE_STATUS_ERROR;
		}

		// check for 'josso_authentication' (could be a POST !)
		string pJossoAuthentication = req->getParameter("josso_authentication");
		if (!pJossoAuthentication.empty()) {

			jk_log(ssoAgent->logger, JK_LOG_DEBUG, "'josso_authentication' received");

			string splashResource = req->getParameter("josso_splash_resource");

			splashResource = req->URLdecode(splashResource);
			if (!splashResource.empty()) {
				string encodedSplashResource = StringUtil::encode64(splashResource);

				jk_log(ssoAgent->logger, JK_LOG_TRACE, "Splash resource %s (encoded %s)", splashResource.c_str(), encodedSplashResource.c_str());
				res->setCookie("JOSSO_SPLASH_RESOURCE", encodedSplashResource, "/");
			}

			// Since the URL is for JOSSO Extension, we need the app. id as parameter
			string pJossoAppId = req->getParameter("josso_partnerapp_id");
			string pJossoUsername = req->getParameter("josso_username");
			string pJossoPassword = req->getParameter("josso_password");
			string gwyLoginUrl (ssoAgent->getGwyLoginUrl());

			if (pJossoUsername.empty()) {
				jk_log(ssoAgent->logger, JK_LOG_ERROR, "No username received as 'josso_username'");
			}

			if (pJossoPassword.empty()) {
				jk_log(ssoAgent->logger, JK_LOG_ERROR, "No password received as 'josso_password'");
			}

			// TODO : Support back to URL (for JOSSO 1.x GWY)
		    // Build output

			// Form header and action
			string authnForm = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" 
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" 
                "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" 
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n" 
                "<body onload=\"document.forms[0].submit()\">\n" 
                "<noscript>\n"
				"<p>\n"
				"<strong>Note:</strong> Since your browser does not support JavaScript,\n"
                "you must press the Continue button once to proceed.\n"
				"</p>\n"
				"</noscript>\n"
				"<form action=\"";
			authnForm.append(gwyLoginUrl.c_str());
			authnForm.append("\" method=\"post\" name=\"usernamePasswordLoginForm\" enctype=\"application/x-www-form-urlencoded\">\n"
                "        <div>");

			// Command
			authnForm.append(
				"              <input type=\"hidden\" value=\"login\" name=\"josso_command\" />\n");

			// Partnerapp id
			authnForm.append(
				"              <input type=\"hidden\" value=\"");
		    authnForm.append(pJossoAppId);
			authnForm.append("\" name=\"josso_partnerapp_id\" />\n"
				"\n");

			// Username
			authnForm.append(
				"              <input type=\"hidden\" value=\"");
		    authnForm.append(pJossoUsername);
			authnForm.append("\" name=\"josso_username\" />\n"
				"\n");
			// Password
			authnForm.append(
				"              <input type=\"hidden\" value=\"");
		    authnForm.append(pJossoPassword);
			authnForm.append("\" name=\"josso_password\" />\n"
				"\n");

			// Submit and end of form
			authnForm.append(
				"              <noscript><input type=\"submit\" value=\"Continue\"/></noscript>\n"
                "        </div>\n"
                "</form>\n"
                "</body>\n"
                "</html>");

			// Write output and return
			if (res->sendContent(authnForm)) {
				rv = HSE_STATUS_SUCCESS;
			} else {
				rv = HSE_STATUS_ERROR;
			}

		}

		// check for 'josso_security_check'
		string pJossoSecurityCheck = req->getParameter("josso_security_check");
		if (!pJossoSecurityCheck.empty()) {

			jk_log(ssoAgent->logger, JK_LOG_DEBUG, "'josso_security_check' received ... ");			

			string assertionId = req->getParameter("josso_assertion_id");
			string ssoSessionId;

			// Get requested original resource, if any
			
			string originalResource = req->getCookie("JOSSO_RESOURCE");
			originalResource = StringUtil::decode64(originalResource);
			
			// Check if we have an assertion ID
			
			if (assertionId.empty()) {
				jk_log(ssoAgent->logger, JK_LOG_TRACE, "'josso_security_check' without assertion (probably failed automatic login)");
				// This is probably a failed automatic login, go back to orignal resource.

				if (!originalResource.empty()) {
					jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Redirecting to %s", originalResource.c_str());
					res->sendRedirect(originalResource);
				} else {
					jk_log(ssoAgent->logger, JK_LOG_ERROR, "No original resource received as COOKIE JOSSO_RESOURCE! ");
					rv = HSE_STATUS_ERROR;
				}

			} else {
				jk_log(ssoAgent->logger, JK_LOG_DEBUG, "'josso_security_check' received, resolving assertion %s", assertionId.c_str());

				if (!ssoAgent->resolveAssertion(assertionId, ssoSessionId, req)) {
					jk_log(ssoAgent->logger, JK_LOG_ERROR, "Cannot resolve assertion %s", assertionId.c_str());
					rv = HSE_STATUS_ERROR;
				} else {

					jk_log(ssoAgent->logger, JK_LOG_TRACE, "Resolved assertion [%s] as SSO Session [%s]", assertionId.c_str(), ssoSessionId.c_str());

					// Create JOSSO SESSION ID Cookie
					string https = req->getServerVariable("HTTPS", MAX_HEADER_SIZE);
					bool secure = false;
					if(https == "on" || https == "ON") secure = true;

					res->setCookie("JOSSO_SESSIONID", ssoSessionId, "/", secure);
					res->setCookie("JOSSO_AUTOLOGIN_REFERER", "-", "/"); // Clean stored referer

					// Retrieve and decode splash resource
					string splashResource = req->getCookie("JOSSO_SPLASH_RESOURCE");
					splashResource = StringUtil::decode64(splashResource);

					if (!splashResource.empty()) {
						jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Redirecting to JOSSO_SPLASH_RESOURCE [%s]", splashResource.c_str());
						res->sendRedirect(splashResource);
					} else if (!originalResource.empty()) {
						jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Redirecting to JOSSO_RESOURCE [%s]", originalResource.c_str());
						res->sendRedirect(originalResource);
					} else {
						jk_log(ssoAgent->logger, JK_LOG_ERROR, "No JOSSO_RESOURCE or JOSSO_SPLASH_RESOURCE received!");
						rv = HSE_STATUS_ERROR;
					}
				}
			}
		}

		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Processed request for URI %s", path.c_str());

	} else {
		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Processed request with ERROR");
	}
	
	// -----------------------------------------------
	// !!! IMPORTAT : CLEAN UP AFTER EACH REQUEST !!!!
	// -----------------------------------------------
	
	delete req;
	delete res;
	
	return rv;
} /* HttpExtensionProc */

BOOL WINAPI TerminateExtension(DWORD dwFlags)
{
    return TRUE;
} /* TerminateExtension */


//--------------------------------------------------------------------------------
// JOSSO DLL entry points
//--------------------------------------------------------------------------------
BOOL WINAPI DllMain(HINSTANCE hInst,    // Instance Handle of the DLL
                    ULONG ulReason,     // Reason why NT called this DLL
                    LPVOID lpReserved)  // Reserved parameter for future use
{
    BOOL fReturn = TRUE;

	UNREFERENCED_PARAMETER(lpReserved);

	syslog(JK_LOG_TRACE_LEVEL, "DllMain() : JOSSO Isapi DLL Main Entry point.  Reason: %d", ulReason);

    switch (ulReason) {
    case DLL_PROCESS_ATTACH:
	    break;

	case DLL_PROCESS_DETACH:
        break;

    default:
        break;
    }

	if (!fReturn)
		syslog(JK_LOG_TRACE_LEVEL, "Return is FALSE!");

    return fReturn;
}

