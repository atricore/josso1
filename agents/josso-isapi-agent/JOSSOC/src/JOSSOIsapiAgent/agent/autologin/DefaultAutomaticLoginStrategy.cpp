#include <JOSSOIsapiAgent/agent/autologin/DefaultAutomaticLoginStrategy.hpp>
#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>

#include "JOSSOIsapiAgent/agent/SSOAgentRequest.hpp"
#include "JOSSOIsapiAgent/agent/SSOAgentResponse.hpp"
#include "JOSSOIsapiAgent/util/StringUtil.hpp"

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

bool DefaultAutomaticLoginStrategy::isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res) {
	jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Processing unauthenticated request, check automatic login");

	// Ignore Extension URI requests
	string autoLoginExecuted = req->getCookie("JOSSO_AUTOMATIC_LOGIN_EXECUTED"); 

	// If we are processing JOSSOIsapi extension, we cannot trigger the auto login process.
	string myUri = req->getServerVariable("URL", MAX_HEADER_SIZE);
	string extensionUri = ssoAgent->getExtensionUri();
	string &path = req->getPath();
	PartnerAppConfig *appCfg = ssoAgent->getPartnerAppConfig(path);
	string splashResource = appCfg->getSplashResource();
	jk_log(ssoAgent->logger, JK_LOG_TRACE, "Processing uri %s (agent:%s)", myUri.c_str(), extensionUri.c_str());

	// Ignore the splash resource and the extension URI
	if (myUri.compare(extensionUri) != 0 && (splashResource.empty() || splashResource.compare(myUri) != 0)) {

		string referer = req->getServerVariable("HTTP_REFERER", MAX_HEADER_SIZE);

		jk_log(ssoAgent->logger, JK_LOG_TRACE, "Processing referer %s", referer.c_str());

		// We have a referer!
		if (!referer.empty()) {
			// If the referer does not begin with our protocol / host, trigger auto login
			string myUrl;
			string host = req->getServerVariable("HTTP_HOST", MAX_HEADER_SIZE);
			string https = req->getServerVariable("HTTPS", MAX_HEADER_SIZE);

			myUrl.append(https == "on" || https == "ON" ? "https://" : "http://");
			myUrl.append(host);

			jk_log(ssoAgent->logger, JK_LOG_TRACE, "Processing referer %s (my url:%s)", referer.c_str(), myUrl.c_str());

			// check if the referer starts with myUrl
			size_t pos = referer.find(myUrl);

			string md5OldReferer = req->getCookie("JOSSO_AUTOLOGIN_REFERER");
			string md5Referer = StringUtil::getHashFromString(referer);

			jk_log(ssoAgent->logger, JK_LOG_TRACE, "received vs previous referer: [%s] [%s]", md5Referer.c_str(), md5OldReferer.c_str());

			// The referer does not match the old referer and it is not a 'local' referer.
			if (md5Referer.compare(md5OldReferer) !=0 && pos != 0) {
				// Store old referer
				res->setCookie("JOSSO_AUTOLOGIN_REFERER", md5Referer, "/");
				// Trigger auto login process
				jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Starting Automatic Login for referer %s (%s)", referer.c_str(), md5Referer.c_str());
				return true;
			} else {
				//jk_log(ssoAgent->logger, JK_LOG_TRACE, "Clean old referer (%s)", md5OldReferer.c_str());
				// TODO : This does not work, because we do not handle this request ....
				res->setCookie("JOSSO_AUTOLOGIN_REFERER", "-", "/"); 
			}
		} else {

			string autoLoginExecuted = req->getCookie("JOSSO_AUTOMATIC_LOGIN_EXECUTED");
			if (autoLoginExecuted.empty() || autoLoginExecuted.compare("-") == 0 ) {
				// Start automatic login
				res->setCookie("JOSSO_AUTOMATIC_LOGIN_EXECUTED", "TRUE", "/");
				if (!referer.empty()) {
					string md5Referer = StringUtil::getHashFromString(referer);
					res->setCookie("JOSSO_AUTOLOGIN_REFERER", md5Referer, "/");
				}
				//jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Starting Automatic Login for the first time");
				return true;
			} else {
				// Clean previous flag
				res->setCookie("JOSSO_AUTOMATIC_LOGIN_EXECUTED", "-", "/");
			}

		}

	}

	return false;
}
