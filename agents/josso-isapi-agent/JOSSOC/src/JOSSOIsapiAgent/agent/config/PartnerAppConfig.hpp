#ifndef PARTNER_APP_CONFIG
#define PARTNER_APP_CONFIG

#include <vector>
#include <string>

using namespace std;

class PartnerAppConfig {

public:
	PartnerAppConfig(const string &id) {
		this->id.assign(id);
	}

	const char * getId() ;
	const char * getKey() ;
	const char * getSplashResource();
	const char * getPartnerAppId();
	const char * getAppLoginUrl();

	void setKey(const string &key);
	void setSplashResource(const string &splashResource);
	void setPartnerAppId(const string &partnerAppId);
	void setAppLoginUrl(const string &appLoginUrl);

protected:
	string id;
	string key;
	vector<string> baseUris;
	vector<string> ignoredUris;
	string splashResource;
	string partnerAppId;
	string appLoginUrl;

	friend class AbstractSSOAgent;
};

#endif