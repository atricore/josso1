#ifndef ISAPI_AGENT_CONFIG
#define ISAPI_AGENT_CONFIG

#include <JOSSOIsapiAgent/agent/config/AgentConfig.hpp>

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>
#include <string>

class IsapiAgentConfig : public AgentConfig {

public:
	IsapiAgentConfig();

protected:

	int iisMajorVersion;
	int iisMinorVersion;
    char extensionUri[INTERNET_MAX_URL_LENGTH];

	friend class IsapiSSOAgent;


};

#endif