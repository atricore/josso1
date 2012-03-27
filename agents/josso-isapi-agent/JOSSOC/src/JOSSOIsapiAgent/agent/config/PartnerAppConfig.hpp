#ifndef PARTNER_APP_CONFIG
#define PARTNER_APP_CONFIG

#include <vector>
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
	const char * getAppLoginUrl();

	void setSplashResource(const string &splashResource);
	void setPartnerAppId(const string &partnerAppId);
	void setAppLoginUrl(const string &appLoginUrl);

protected:
	string id;
	string baseUri;
	vector<string> ignoredUris;
	string splashResource;
	string partnerAppId;
	string appLoginUrl;

	friend class AbstractSSOAgent;
};

#endif