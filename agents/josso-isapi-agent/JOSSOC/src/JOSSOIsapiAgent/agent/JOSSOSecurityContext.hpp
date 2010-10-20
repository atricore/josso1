#ifndef JOSSO_SECURITY_CONTEXT_DEF
#define JOSSO_SECURITY_CONTEXT_DEF

#include <utility>
#include <string>
#include <map>
#include <vector>
#include <list>

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>

using namespace std;

class JOSSOSecurityContext {

public:

	JOSSOSecurityContext();

	bool isAuthenticated();

	bool hasRole(string roleName);

	vector<string> getRoles() { return this->roles; }

	map<string, string> getUserProperties() { return this->usrProperties; }

	string getUserProperty(string name);

	string getPrincipal();

	string getOriginalResource();

	void setAuthN(string originalResource, string ssoSessionId, string principal, map<string, string> usrProperties, vector<string> roles);

	void setLogger(jk_logger_t * l) { this->logger = l; }

protected:

	bool authn;

	string originalResource;

	string ssoSessionId;

	string principal;

	vector<string> roles;

	map<string, string> usrProperties;

	jk_logger_t * logger;

};

#endif
