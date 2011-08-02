#ifndef DEFAULT_AUTOMATIC_LOGIN_STRATEGY_DEF
#define DEFAULT_AUTOMATIC_LOGIN_STRATEGY_DEF

#include <JOSSOIsapiAgent/agent/autologin/AbstractAutomaticLoginStrategy.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>

#include <string>

using namespace std;

class DefaultAutomaticLoginStrategy : public AbstractAutomaticLoginStrategy {
public:

	DefaultAutomaticLoginStrategy() {}

	DefaultAutomaticLoginStrategy(const string &mode) : AbstractAutomaticLoginStrategy(mode) {}

	bool isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res);

protected:

	vector<string> ignoredReferers;

	friend class IsapiSSOAgent;

};

#endif