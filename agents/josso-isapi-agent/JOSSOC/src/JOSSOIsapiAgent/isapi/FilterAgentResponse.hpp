#ifndef FILTER_AGENT_RESPONSE
#define FILTER_AGENT_RESPONSE

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>
// #include <strsafe.h>

#include <string>
#include <list>
#include <map>
#include <utility>

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>
#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>

using namespace std;

/**
 * ISS Isapi Filter based agent request
 */
class FilterAgentResponse : public SSOAgentResponse {

public:

	FilterAgentResponse(HTTP_FILTER_CONTEXT * pfc, DWORD NotificationType, VOID* pvNotification, jk_logger_t * l);

	~FilterAgentResponse();

	string getServerVariable(string name, DWORD cbSize);

	bool isCommitted() { return committed; }

	bool flushHeaders() ;

	bool sendContent(string content);

protected:

	bool startResponse(int status, string reason, list<pair<string, string>> headers);

	bool writeContent(const char * content, size_t length);

private:

	bool committed;

	// IIS HTTP Filter Context 
	HTTP_FILTER_CONTEXT *pfc;

	// IIS Notificatio type
	DWORD NotificationType;

	// IIS Notification
	VOID* pvNotification;

};

#endif