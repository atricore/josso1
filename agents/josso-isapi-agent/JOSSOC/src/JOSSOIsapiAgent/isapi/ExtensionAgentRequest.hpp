#ifndef EXTENSION_AGENT_REQUEST
#define EXTENSION_AGENT_REQUEST

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>
// #include <strsafe.h>

#include <string>
#include <map>

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>
#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>

using namespace std;

class ExtensionAgentRequest : public SSOAgentRequest {

public:
	ExtensionAgentRequest(LPEXTENSION_CONTROL_BLOCK lpEcb, jk_logger_t * l);

	~ExtensionAgentRequest();

	string getQueryString();

	string getServerVariable(string name, DWORD cbSize);

	bool exportSecurityContext(JOSSOSecurityContext & ctx);


private:

	string uri;

	string qryString;

	LPEXTENSION_CONTROL_BLOCK lpEcb;


};

#endif