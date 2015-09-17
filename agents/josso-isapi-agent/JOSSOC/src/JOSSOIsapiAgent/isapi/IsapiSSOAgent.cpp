#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include <JOSSOIsapiAgent/isapi/IsapiSSOAgent.hpp>
#include <JOSSOIsapiAgent/isapi/IsapiAgentConfig.hpp>

#include "JOSSOIsapiAgent/util/simpleini/SimpleIni.h"
#include "JOSSOIsapiAgent/util/StringUtil.hpp"

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif


// Initialize some constants

const char *IsapiSSOAgent::JOSSO_REGISTRY_LOCATION = "Software\\Atricore\\JOSSO Isapi Agent\\1.8";

const char *IsapiSSOAgent::JOSSO_LOG_FILE = "LogFile";

const char *IsapiSSOAgent::JOSSO_LOG_LEVEL = "LogLevel";

const char *IsapiSSOAgent::JOSSO_AGENT_CONFIG = "AgentConfigFile";

const char *IsapiSSOAgent::JOSSO_EXTENSION_URI = "ExtensionUri";

const char *IsapiSSOAgent::W3SVC_REGISTRY_KEY = "SYSTEM\\CurrentControlSet\\Services\\W3SVC\\Parameters";

/**
 * 
 **/
bool IsapiSSOAgent::start() {

	bool ok = true;

	syslog(JK_LOG_INFO_LEVEL, "Starting JOSSO Isapi Agent");

	// Initialize JOSSO Isapi Agent
	ok = ok & AbstractSSOAgent::start();

	return ok;

}

bool IsapiSSOAgent::stop() {
	bool ok = true;
	
	if(!jk_close_file_logger(&logger))
		ok = false;

	ok = ok && AbstractSSOAgent::stop();

	return ok;
}
bool IsapiSSOAgent::requestLogin(SSOAgentRequest * req, SSOAgentResponse * res, PartnerAppConfig * appCfg, bool optional) {

	string agentLoginUrl (this->getExtensionUri());
	if (optional)
		// If login fails the gateway will silently return to the back_to resource.
		agentLoginUrl.append("?josso_login_optional");
	else
		agentLoginUrl.append("?josso_login");

	agentLoginUrl.append("&josso_partnerapp_id=");
	agentLoginUrl.append(appCfg->getPartnerAppId());

	string &path = req->getPath();
	string encodedPath = StringUtil::encode64(path);
	string encodedSplashResource = StringUtil::encode64(appCfg->getSplashResource());

	jk_log(this->logger, JK_LOG_TRACE, "Encoded PATH %s", encodedPath.c_str());
	jk_log(this->logger, JK_LOG_TRACE, "Encoded SPLASH RESOURCE %s", encodedSplashResource.c_str());
					
	res->setCookie("JOSSO_RESOURCE", encodedPath, "/", false);
	res->setCookie("JOSSO_SPLASH_RESOURCE", encodedSplashResource, "/", false);
	
	res->sendRedirect(agentLoginUrl);

	return TRUE;

}

char * IsapiSSOAgent::getExtensionUri() {
	return ((IsapiAgentConfig *)this->agentConfig)->extensionUri;
}



string IsapiSSOAgent::buildGwyLoginUrl(SSOAgentRequest *req) {

	string url (getGwyLoginUrl());

	return this->buildGwyLoginUrl(req, url);

}

string IsapiSSOAgent::buildGwyLoginUrl(SSOAgentRequest *req, string url) {

	string backToBaseUrl (getBackToBaseUrl());
    string host = req->getServerVariable("HTTP_HOST", MAX_HEADER_SIZE);
	string https = req->getServerVariable("HTTPS", MAX_HEADER_SIZE);

	// Add parameter to URL
	if (url.find("?") == string::npos)
		url.append("?");
	else
		url.append("&");
	url.append("&josso_back_to=");

	// Add parameter value to URL
	if (!backToBaseUrl.empty()) {
		url.append(backToBaseUrl);
	} else {
		url.append(https == "on" || https == "ON" ? "https://" : "http://");
		url.append(host);
	}

	// Add extension ACS endpont
	url.append(getExtensionUri());
	url.append("%3Fjosso_security_check&amp;");
	url.append("josso_partnerapp_host=");
	url.append(host.c_str()); // TODO : Take host from baseBackTo URL if any ?

	return url;

}

string IsapiSSOAgent::buildGwyLogoutUrl(SSOAgentRequest *req) {

	string backToBaseUrl (getBackToBaseUrl());
	string url (getGwyLogoutUrl());
	string host = req->getServerVariable("HTTP_HOST", MAX_HEADER_SIZE);
	string https = req->getServerVariable("HTTPS", MAX_HEADER_SIZE);

	if (url.find("?") == string::npos)
		url.append("?");
	else
		url.append("&");

	// Add parameter to URL
	url.append("josso_back_to=");

	// Add parameter value to URL
	if (!backToBaseUrl.empty()) {
		url.append(backToBaseUrl);
	} else {
		url.append(https == "on" || https == "ON" ? "https://" : "http://");
		url.append(host);
	}

	return url;

}

SSOAgentRequest *IsapiSSOAgent::initIsapiFilterRequest(HTTP_FILTER_CONTEXT *pfc, 
										   DWORD NotificationType, 
										   VOID* pvNotification) {

	jk_log(logger, JK_LOG_TRACE, "Creating FilterAgentRequest instance");
	SSOAgentRequest * req = new FilterAgentRequest(pfc, NotificationType, pvNotification, logger);
	jk_log(logger, JK_LOG_TRACE, "Creating FilterAgentRequest instance ... OK");

	return req;
}

SSOAgentResponse *IsapiSSOAgent::initIsapiFilterResponse(HTTP_FILTER_CONTEXT *pfc, 
										   DWORD NotificationType, 
										   VOID* pvNotification) {

	jk_log(logger, JK_LOG_TRACE, "Creating FilterAgentResponse instance");
	SSOAgentResponse * res = new FilterAgentResponse(pfc, NotificationType, pvNotification, logger);
	jk_log(logger, JK_LOG_TRACE, "Creating FilterAgentResponse instance ... OK");

	return res;
}

SSOAgentResponse *IsapiSSOAgent::initIsapiExtensionResponse(LPEXTENSION_CONTROL_BLOCK lpEcb) {
	jk_log(logger, JK_LOG_TRACE, "Creating ExtensionAgentResponse instance");
	SSOAgentResponse * res = new ExtensionAgentResponse(lpEcb, logger);
	jk_log(logger, JK_LOG_TRACE, "Creating ExtensionAgentResponse instance ... OK");

	return res;
}

SSOAgentRequest *IsapiSSOAgent::initIsapiExtensionRequest(LPEXTENSION_CONTROL_BLOCK lpEcb) {
	jk_log(logger, JK_LOG_TRACE, "Creating ExtensionAgentRequest instance");
	SSOAgentRequest * req = new ExtensionAgentRequest(lpEcb, logger);
	jk_log(logger, JK_LOG_TRACE, "Creating ExtensionAgentRequest instance ... OK");

	return req;
}

const char *IsapiSSOAgent::getRequester(SSOAgentRequest *req) {

	string originalResource = req->getPath();
	string &host = req->getHost();

	// Look for original resource as JOSSO_RESOURCE:
	if (originalResource.empty() || !originalResource.find(this->getExtensionUri())) {
		originalResource = req->getCookie("JOSSO_RESOURCE");
		originalResource = StringUtil::decode64(originalResource);
	}

	// Look for original resource as JOSSO_SPLASH_RESOURCE:
	if (originalResource.empty()) {
		originalResource = req->getCookie("JOSSO_SPLASH_RESOURCE");
		originalResource = StringUtil::decode64(originalResource);
	}

	jk_log(req->logger, JK_LOG_TRACE, "Looking application definition for [%s]", originalResource.c_str());

	if (!originalResource.empty()) {
		PartnerAppConfig *appCfg = getPartnerAppConfig(host, originalResource);
		if (appCfg != NULL) {
			jk_log(req->logger, JK_LOG_TRACE, "Found application definition %s for [%s]",
				appCfg->getId(), originalResource.c_str());

			return appCfg->getPartnerAppId();
		} else {
			jk_log(req->logger, JK_LOG_ERROR, "Cannot find application config for path [%s]", originalResource.c_str());
		}
	} else {
		// TODO : This is just a work-around ... !
		jk_log(req->logger, JK_LOG_WARNING, "Cannot find PATH, JOSSO_RESOURCE or JOSSO_SPLASH_RESOURCE, unable to determine Partner Application ID");
		PartnerAppConfig *appCfg = getDefaultPartnerAppConfig();
		return appCfg->getPartnerAppId();
	}
	 
	// This makes IIS crash, and the app pool is disabled after a few crashes!
	// return NULL
	
}

// ---------------------------------------------------------------------------------------------

AgentConfig *IsapiSSOAgent::createAgentConfig() {
	return new IsapiAgentConfig();
}

bool IsapiSSOAgent::configureAgent(AgentConfig *config) {

    bool ok = true;
    HKEY hkey;
	
	syslog(JK_LOG_TRACE_LEVEL, "Starting JOSSO Isapi Agent Configuration ... ");

	IsapiAgentConfig *cfg = (IsapiAgentConfig *) config; 
	if (cfg == NULL) {
		syslog(JK_LOG_ERROR_LEVEL, "Configuration is NULL");
	}
	
	cfg->logLevel = JK_LOG_INFO_LEVEL;

	syslog(JK_LOG_DEBUG_LEVEL, "Opening Windows Registry [%s]", JOSSO_REGISTRY_LOCATION);

    long rc = RegOpenKeyEx(HKEY_LOCAL_MACHINE, JOSSO_REGISTRY_LOCATION,
                           (DWORD)0, KEY_READ, &hkey);
    if (ERROR_SUCCESS != rc) {
		syslog(JK_LOG_ERROR_LEVEL, "Cannot open Windows Registry, return code : %d" + rc);
        return false;
    }

	// Log File
    ok = ok && getRegistryValue(hkey, JOSSO_LOG_FILE, cfg->logFile, sizeof(cfg->logFile));
	syslog(JK_LOG_DEBUG_LEVEL, "%s [%s]", JOSSO_LOG_FILE, cfg->logFile);

    // Log Level (optional)
	char tmpbuf[MAX_PATH];
    if (getRegistryValue(hkey, JOSSO_LOG_LEVEL, tmpbuf, sizeof(tmpbuf))) {
        cfg->logLevel = jk_parse_log_level(tmpbuf);
		syslog(JK_LOG_DEBUG_LEVEL, "%s [%d] [%s]", JOSSO_LOG_LEVEL, cfg->logLevel, tmpbuf);
    }
	
	// Extension URI
    ok = ok && getRegistryValue(hkey, JOSSO_EXTENSION_URI, cfg->extensionUri, sizeof(cfg->extensionUri));
	syslog(JK_LOG_DEBUG_LEVEL, "%s [%s]", JOSSO_EXTENSION_URI, cfg->extensionUri);

	// Agent configuraiton file 
    ok = ok && getRegistryValue(hkey, JOSSO_AGENT_CONFIG, cfg->agentConfigFile, sizeof(cfg->agentConfigFile));
	syslog(JK_LOG_DEBUG_LEVEL, "%s [%s]", JOSSO_AGENT_CONFIG, cfg->agentConfigFile);


    RegCloseKey(hkey);

	// Now that we have done some initialization , call our parent class
	ok = ok && AbstractSSOAgent::configureAgent(cfg);
    
    return ok;
}



bool IsapiSSOAgent::getIISInfo() {
    HKEY hkey;
    long rc;
    bool rv = false;

    int major = 0;
    int minor = 0;

    /* Retrieve the IIS version Major/Minor */
    rc = RegOpenKeyEx(HKEY_LOCAL_MACHINE,
                      W3SVC_REGISTRY_KEY, (DWORD) 0, KEY_READ, &hkey);

    if (ERROR_SUCCESS != rc)
		return false;
	
	
	if (getRegistryNumber(hkey, "MajorVersion", &major)) {
		if (major > 4) {
    		if (getRegistryNumber(hkey, "MinorVersion", &minor) == JK_TRUE) {
	            rv = true;
				syslog(JK_LOG_DEBUG_LEVEL, "IIS Server version detected %d.%d", major, minor);
			}
		}
	}

	CloseHandle(hkey);

	
    return rv;
}

bool IsapiSSOAgent::getRegistryValue(HKEY hkey, const char *tag, char *b, DWORD sz)
{    
	DWORD type = 0;
    LONG lrc;

    sz = sz - 1; /* Reserve space for RegQueryValueEx to add null terminator */
    b[sz] = '\0'; /* Null terminate in case RegQueryValueEx doesn't */

    lrc = RegQueryValueEx(hkey, tag, (LPDWORD) 0, &type, (LPBYTE) b, &sz);
    if ((ERROR_SUCCESS != lrc) || (type != REG_SZ)) {
        return false;
    }

    return true;
}


bool IsapiSSOAgent::getRegistryNumber(HKEY hkey,
                                      const char *tag, int *val)
{
    DWORD type = 0;
    DWORD data = 0;
    DWORD sz   = sizeof(DWORD);
    LONG lrc;

    lrc = RegQueryValueEx(hkey, tag, (LPDWORD)0, &type, (LPBYTE)&data, &sz);
    if ((ERROR_SUCCESS != lrc) || (type != REG_DWORD)) {
        return false;
    }

    *val = (int)data;

    return true;
}
