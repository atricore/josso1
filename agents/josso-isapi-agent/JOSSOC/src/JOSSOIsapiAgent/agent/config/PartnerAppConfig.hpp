#ifndef PARTNER_APP_CONFIG
#define PARTNER_APP_CONFIG

#include <string>

using namespace std;

class PartnerAppConfig {

public:
	PartnerAppConfig(const string &id, const string &baseUri) {
		this->id.assign(id);
		this->baseUri.assign(baseUri);
	}

	const char * getId() ;
	const char * getBaseUri() ;
	const char * getSplashResource();
	const char * getPartnerAppId();

	void setSplashResource(const string &splashResource);
	void setPartnerAppId(const string &partnerAppId);

protected:
	string id;
	string baseUri;
	string splashResource;
	string partnerAppId;

	friend class AbstractSSOAgent;
};

#endif