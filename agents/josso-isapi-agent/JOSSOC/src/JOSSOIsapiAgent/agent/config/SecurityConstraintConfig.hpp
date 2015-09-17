#ifndef SECURITY_CONSTRAINT_CONFIG
#define SECURITY_CONSTRAINT_CONFIG

#include "JOSSOIsapiAgent/isapi/josso_isapi.h"

#include <vector>
#include <string>

using namespace std;

class SecurityConstraintConfig {

public:
	SecurityConstraintConfig(const string &i) {
		id.assign(i);
	}

	string priority;

	string host;

protected:

	string id;

	vector<string> roles;

	vector<string> baseUris;

	friend class AbstractSSOAgent;

};

#endif