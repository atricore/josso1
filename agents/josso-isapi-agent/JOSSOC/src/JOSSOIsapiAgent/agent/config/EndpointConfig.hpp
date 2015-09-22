#ifndef ENDPOINT_CONSTRAINT_CONFIG
#define ENDPOINT_CONSTRAINT_CONFIG

#include "JOSSOIsapiAgent/isapi/josso_isapi.h"

#include <wininet.h>

using namespace std;

class EndpointConfig {

public:
	EndpointConfig() {
		
	}

	char* getGatewayEndpoint() { return gatewayEndpoint; }

	bool isSecureTransport() { return secureTransport; }

	bool isSslSkipHostCheck() { return sslSkipHostCheck; }

	bool isSslAllowExpiredCerts() { return sslAllowExpiredCerts; }

	char *getUserId() { return userId; }

	char *getPassword() { return password; }

protected:

	string id;

	char gatewayEndpoint[INTERNET_MAX_URL_LENGTH];

	bool secureTransport;

	bool sslSkipHostCheck;

	bool sslAllowExpiredCerts;

	char userId[INTERNET_MAX_USER_NAME_LENGTH];

	char password[INTERNET_MAX_PASSWORD_LENGTH];


	friend class AbstractSSOAgent;

};

#endif