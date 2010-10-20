#ifndef FILTER_AGENT_REQUEST
#define FILTER_AGENT_REQUEST

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>
// #include <strsafe.h>

#include <string>
#include <map>

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>
#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>

using namespace std;

/**
 * ISS Isapi Filter based agent request
 */
class FilterAgentRequest : public SSOAgentRequest {

public:

	FilterAgentRequest(HTTP_FILTER_CONTEXT * pfc, DWORD NotificationType, VOID* pvNotification, jk_logger_t * l);

	~FilterAgentRequest();

	string getQueryString();

	string getServerVariable(string name, DWORD cbSize);

	bool exportSecurityContext(JOSSOSecurityContext & ctx);

	string normalizeServerVariableName(string svarName);

protected:
	
	bool setHeader(string name, string value);

private:


	// IIS HTTP Filter Context 
	HTTP_FILTER_CONTEXT *pfc;

	// IIS Notificatio type
	DWORD NotificationType;

	// IIS Notification
	VOID* pvNotification;

	string queryString;

};

#endif