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
	const char * getDefaultResource();

	void setKey(const string &key);
	void setSplashResource(const string &splashResource);
	void setPartnerAppId(const string &partnerAppId);
	void setAppLoginUrl(const string &appLoginUrl);
	void setDefaultResource(const string &defaultResource);

protected:
	string id;
	string key;
	vector<string> baseUris;
	vector<string> ignoredUris;
	string splashResource;
	string partnerAppId;
	string appLoginUrl;
	string defaultResource;

	friend class AbstractSSOAgent;
};

#endif