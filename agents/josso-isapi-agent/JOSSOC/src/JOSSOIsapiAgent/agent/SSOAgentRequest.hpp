#ifndef SSO_AGENT_REQUEST_DEF
#define SSO_AGENT_REQUEST_DEF


#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>
#include <utility>
#include <string>
#include <map>
#include <vector>

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>

#include <JOSSOIsapiAgent/util/StringUtil.hpp>
#include <JOSSOIsapiAgent/agent/JOSSOSecurityContext.hpp>

using namespace std;

class SSOAgentRequest {

public:


	virtual string getPath() ;

	virtual string getHost() ;

	virtual string getQueryString() =0;

	virtual string getHeader(string name);

	virtual string getCookie(string name);

	virtual string getParameter(string name);

	virtual string getMethod() =0;

	virtual string getContentType() =0;

	virtual DWORD getBodySize() =0;

	virtual LPBYTE getBody() =0;

	virtual bool isUserInRole(string roleName);

	virtual bool isAuthenticated();

	virtual string getRemoteUser();

	virtual string getServerVariable(string name, DWORD cbSize) = 0;

	virtual bool exportSecurityContext(JOSSOSecurityContext & ctx) = 0;

	static const string EMPTY_PARAM;

	static const string EMPTY_STR;

	static const string MULTIPART_FORM_DATA;

	std::string URLdecode(const std::string& l);


	

protected:

	jk_logger_t *logger;

	string uri;

	map<string, string> params;

	map<string, string> cookies;

	JOSSOSecurityContext secCtx;

	bool parseQueryString(string qryStr);

	bool parseMimeString(string contentType, string mimeStr);

	bool parsePostData(LPBYTE body, DWORD bodySize, string contentType);

	friend class AbstractSSOAgent;
	friend class IsapiSSOAgent;

};

#endif