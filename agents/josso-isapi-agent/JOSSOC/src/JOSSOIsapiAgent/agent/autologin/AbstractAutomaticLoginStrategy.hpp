#ifndef ABSTRACT_AUTOMATIC_LOGIN_STRATEGY_DEF
#define ABSTRACT_AUTOMATIC_LOGIN_STRATEGY_DEF

#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>
#include <JOSSOIsapiAgent/agent/AbstractSSOAgent.hpp>

#include <JOSSOIsapiAgent/util/jk/common/jk_logger.h>
#include <JOSSOIsapiAgent/util/jk/common/jk_util.h>

#include <string>

using namespace std;

class AbstractAutomaticLoginStrategy {
public:

	// JK Logger 
	static jk_logger_t *logger;

	AbstractAutomaticLoginStrategy() {}

	AbstractAutomaticLoginStrategy(const string &mode) {
		this->mode.assign(mode);
	}

	const char * getMode();

	void setMode(const string &mode);

	AbstractSSOAgent * getSSOAgent();

	void setSSOAgent(AbstractSSOAgent *ssoAgent);

	virtual bool isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res) =0;

protected:

	string mode;
	
	AbstractSSOAgent *ssoAgent;
};

#endif