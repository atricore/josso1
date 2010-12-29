#ifndef SSO_AGENT_RESPONSE_DEF
#define SSO_AGENT_RESPONSE_DEF

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>
#include <string>
#include <list>
#include <utility>

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>

using namespace std;

class SSOAgentResponse {

public:

	~SSOAgentResponse();

	virtual bool sendRedirect(string url);

	virtual bool sendStatus(int status, string reason);

	virtual bool addHeader(string name, string value) ;

	virtual bool setCookie(string name, string value, string path);

	virtual bool setCookie(string name, string value, string path, bool secure);

	virtual bool flushHeaders() =0;

    virtual bool isCommitted() =0;

	// TODO : Add a client writing operation !

protected:

	static const char * HTML_ERROR_HEAD; 

	static const char * HTML_ERROR_BODY_FMT;

	static const char * HTML_ERROR_TAIL;


	list<pair<string, string>> headers;

	jk_logger_t *logger;

	virtual bool startResponse(int status, string reason, list<pair<string, string>> headers) =0;

	virtual bool writeContent(char * content, size_t length) =0;



};

#endif