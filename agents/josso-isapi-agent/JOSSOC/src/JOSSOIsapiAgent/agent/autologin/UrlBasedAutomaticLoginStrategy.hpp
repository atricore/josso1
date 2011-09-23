#ifndef URL_BASED_AUTOMATIC_LOGIN_STRATEGY_DEF
#define URL_BASED_AUTOMATIC_LOGIN_STRATEGY_DEF

#include <JOSSOIsapiAgent/agent/autologin/AbstractAutomaticLoginStrategy.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>

#include <vector>
#include <string>

using namespace std;
/**
 * Trigger automatic login unless the requested URI matches some of the configured url patterns
 */
class UrlBasedAutomaticLoginStrategy : public AbstractAutomaticLoginStrategy {
public:

	UrlBasedAutomaticLoginStrategy() {}

	UrlBasedAutomaticLoginStrategy(const string &mode) : AbstractAutomaticLoginStrategy(mode) {}

	void addUrlPattern(const string &urlPattern);

	bool isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res);

protected:

	vector<string> urlPatterns;

	friend class AbstractSSOAgent;
};

#endif