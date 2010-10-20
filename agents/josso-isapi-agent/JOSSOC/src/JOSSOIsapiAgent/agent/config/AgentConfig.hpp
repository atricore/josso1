#ifndef AGENT_CONFIG
#define AGENT_CONFIG

#include "JOSSOIsapiAgent/isapi/josso_isapi.h"

#include "JOSSOIsapiAgent/agent/config/PartnerAppConfig.hpp"
#include "JOSSOIsapiAgent/agent/config/SecurityConstraintConfig.hpp"

#include <wininet.h>
#include <list>

using namespace std;

class AgentConfig {

public:

	AgentConfig();

	char* getGatewayLoginUrl() { return gatewayLoginUrl; }

	char* getGatewayEndpoint() { return gatewayEndpoint; }
	
	char* getSessionManagerServicePath() { return sessionManagerServicePath; }

	char* getIdentityManagerServicePath() { return identityManagerServicePath; }

	char* getIdentityProviderServicePath() { return identityProviderServicePath; }

	long  getSessionAccessMinInterval() { return sessionAccessMinInterval; }
	
	long  getCacheCleanupMinInterval() { return cacheCleanupMinInterval; }

protected:

	char logFile[MAX_PATH + 2];

	int  logLevel;

	char agentConfigFile[MAX_PATH + 2];

	char gatewayLoginUrl[INTERNET_MAX_URL_LENGTH];

	char gatewayEndpoint[INTERNET_MAX_URL_LENGTH];

	char sessionManagerServicePath[INTERNET_MAX_URL_LENGTH];

	char identityManagerServicePath[INTERNET_MAX_URL_LENGTH];

	char identityProviderServicePath[INTERNET_MAX_URL_LENGTH];

	long sessionAccessMinInterval;
	
	long cacheCleanupMinInterval;

	list<SecurityConstraintConfig> secConstraints;

	list<PartnerAppConfig> apps;

	friend class AbstractSSOAgent;
};

#endif