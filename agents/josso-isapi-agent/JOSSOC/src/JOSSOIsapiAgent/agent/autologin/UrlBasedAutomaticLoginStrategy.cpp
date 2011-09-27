#include <JOSSOIsapiAgent/agent/autologin/UrlBasedAutomaticLoginStrategy.hpp>
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

void UrlBasedAutomaticLoginStrategy::addUrlPattern(const string &urlPattern) {
	this->urlPatterns.push_back(urlPattern);
}

/**
 * This strategy returns false if the accessed URL matches any of the configured URL patterns.
 * The patterns are regular expressions.
 */
bool UrlBasedAutomaticLoginStrategy::isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res) {
	bool autoLoginRequired = true;
	string requestUri = req->getPath();
	vector<string>::const_iterator urlPattern;
	for(urlPattern = urlPatterns.begin() ; urlPattern != urlPatterns.end() ; urlPattern++) {
		bool matched = ssoAgent->match(requestUri, *urlPattern);
		jk_log(ssoAgent->logger, JK_LOG_TRACE, "Check requested URI [%s] againt ignored pattern [%s] : %d" , requestUri.c_str(), (*urlPattern).c_str(), matched);

		if (matched) {
			jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Autologin is not required! Ignored url pattern: %s", requestUri.c_str());
			autoLoginRequired = false;
			break;
		}
	}

	return autoLoginRequired;
}
