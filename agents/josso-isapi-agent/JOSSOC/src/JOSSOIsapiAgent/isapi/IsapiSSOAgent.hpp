#ifndef ISAPI_SSO_AGENT_DEF
#define ISAPI_SSO_AGENT_DEF

#include <JOSSOIsapiAgent/agent/AbstractSSOAgent.hpp>
#include <JOSSOIsapiAgent/isapi/FilterAgentRequest.hpp>
#include <JOSSOIsapiAgent/isapi/FilterAgentResponse.hpp>
#include <JOSSOIsapiAgent/isapi/ExtensionAgentRequest.hpp>
#include <JOSSOIsapiAgent/isapi/ExtensionAgentResponse.hpp>
#include <JOSSOIsapiAgent/isapi/IsapiAgentConfig.hpp>

#include <wininet.h>

extern "C" {
#include <JOSSOIsapiAgent/util/jk/common/jk_logger.h>
#include <JOSSOIsapiAgent/util/jk/common/jk_util.h>
}

class IsapiSSOAgent  : public AbstractSSOAgent {

public:

	// Windows Registry Configuration properties:

	static const char *JOSSO_REGISTRY_LOCATION;

	static const char *JOSSO_LOG_FILE;

	static const char *JOSSO_LOG_LEVEL;

	static const char *JOSSO_AGENT_CONFIG;

	static const char *JOSSO_EXTENSION_URI;

	static const char *W3SVC_REGISTRY_KEY;

	/**
	 * Isapi Start will initialize this agent accessing windows registry information.
	 */
	bool start();

	bool stop();

	char * getExtensionUri();

	bool requestLogin(SSOAgentRequest * req, SSOAgentResponse * res, PartnerAppConfig * cfg, bool optional);

	string buildGwyLoginUrl(SSOAgentRequest *req);

	string buildGwyLoginUrl(SSOAgentRequest *req, string url);

	string buildGwyLogoutUrl(SSOAgentRequest *req);

	SSOAgentRequest *initIsapiFilterRequest(HTTP_FILTER_CONTEXT *pfc, 
										   DWORD NotificationType, 
										   VOID* pvNotification);

	SSOAgentResponse *initIsapiFilterResponse(HTTP_FILTER_CONTEXT *pfc, 
										   DWORD NotificationType, 
										   VOID* pvNotification);

	SSOAgentRequest *initIsapiExtensionRequest(LPEXTENSION_CONTROL_BLOCK lpEcb);

	SSOAgentResponse *initIsapiExtensionResponse(LPEXTENSION_CONTROL_BLOCK lpEcb);

	const char *getRequester(SSOAgentRequest *req);

protected:

    AgentConfig *createAgentConfig() ;

    bool configureAgent(AgentConfig *cfg);

	bool getIISInfo();

	bool getRegistryValue(HKEY hkey, const char *tag, char *b, DWORD sz);

	bool getRegistryNumber(HKEY hkey, const char *tag, int *val);

};

#endif