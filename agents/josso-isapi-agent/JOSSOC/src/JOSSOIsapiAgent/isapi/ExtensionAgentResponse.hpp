#ifndef EXTENSION_AGENT_RESPONSE
#define EXTENSION_AGENT_RESPONSE

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>
// #include <strsafe.h>

#include <string>
#include <map>
#include <list>
#include <utility>

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>
#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>

using namespace std;

class ExtensionAgentResponse : public SSOAgentResponse {

public:
	ExtensionAgentResponse(LPEXTENSION_CONTROL_BLOCK lpEcb, jk_logger_t * l);

	~ExtensionAgentResponse();

	bool flushHeaders();

	bool isCommitted() { return committed; }

	string getServerVariable(string name, DWORD cbSize);

	bool sendContent(string content);

protected:

	bool startResponse(int status, string reason, list<pair<string, string>> headers);

	bool writeContent(const char * content, size_t length);


private:

	bool committed;

	LPEXTENSION_CONTROL_BLOCK lpEcb;


};

#endif