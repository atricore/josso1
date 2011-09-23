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

	char* getGatewayLogoutUrl() { return gatewayLogoutUrl; }

	char* getGatewayEndpoint() { return gatewayEndpoint; }
	
	char* getSessionManagerServicePath() { return sessionManagerServicePath; }

	char* getIdentityManagerServicePath() { return identityManagerServicePath; }

	char* getIdentityProviderServicePath() { return identityProviderServicePath; }

	long  getSessionAccessMinInterval() { return sessionAccessMinInterval; }
	
	long  getCacheCleanupMinInterval() { return cacheCleanupMinInterval; }

	bool isSecureTransport() { return secureTransport; }

	bool getSoapTransportTimeout() { return soapTransportTimeout; }

	bool isSslSkipHostCheck() { return sslSkipHostCheck; }

	bool isSslAllowExpiredCerts() { return sslAllowExpiredCerts; }

	char *getUserId() { return userId; }

	char *getPassword() { return password; }

protected:

	char logFile[MAX_PATH + 2];

	int  logLevel;

	char agentConfigFile[MAX_PATH + 2];

	char gatewayLoginUrl[INTERNET_MAX_URL_LENGTH];

	char gatewayLogoutUrl[INTERNET_MAX_URL_LENGTH];

	char gatewayEndpoint[INTERNET_MAX_URL_LENGTH];

	char sessionManagerServicePath[INTERNET_MAX_URL_LENGTH];

	char identityManagerServicePath[INTERNET_MAX_URL_LENGTH];

	char identityProviderServicePath[INTERNET_MAX_URL_LENGTH];

	long sessionAccessMinInterval;
	
	long cacheCleanupMinInterval;

	bool secureTransport;

	long soapTransportTimeout;

	list<SecurityConstraintConfig> secConstraints;

	list<PartnerAppConfig> apps;

	char caFile[MAX_PATH + 2];

	bool sslSkipHostCheck;

	bool sslAllowExpiredCerts;

	char userId[INTERNET_MAX_USER_NAME_LENGTH];

	char password[INTERNET_MAX_PASSWORD_LENGTH];

	friend class AbstractSSOAgent;
};

#endif