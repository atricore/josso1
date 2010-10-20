#ifndef ROBOT_DEF
#define ROBOT_DEF

#include <string>

using namespace std;

class Robot {
public:

	const char *getId();
	void setId(const string &id);

	const char *getName();
	void setName(const string &name);

	const char *getCoverUrl();
	void setCoverUrl(const string &coverUrl);

	const char *getDetailsUrl();
	void setDetailsUrl(const string &detailsUrl);

	const char *getOwnerName();
	void setOwnerName(const string &ownerName);

	const char *getOwnerUrl();
	void setOwnerUrl(const string &ownerUrl);

	const char *getOwnerEmail();
	void setOwnerEmail(const string &ownerEmail);

	const char *getStatus();
	void setStatus(const string &status);

	const char *getPurpose();
	void setPurpose(const string &purpose);

	const char *getType();
	void setType(const string &type);

	const char *getPlatform();
	void setPlatform(const string &platform);

	const char *getAvailability();
	void setAvailability(const string &availability);

	const char *getExclusion();
	void setExclusion(const string &exclusion);

	const char *getExclusionUserAgent();
	void setExclusionUserAgent(const string &exclusionUserAgent);

	const char *getNoindex();
	void setNoindex(const string &noindex);

	const char *getHost();
	void setHost(const string &host);

	const char *getFrom();
	void setFrom(const string &from);

	const char *getUserAgent();
	void setUserAgent(const string &userAgent);

	const char *getLanguage();
	void setLanguage(const string &language);

	const char *getDescription();
	void setDescription(const string &description);

	const char *getHistory();
	void setHistory(const string &history);

	const char *getEnvironment();
	void setEnvironment(const string &environment);

	const char *getModifiedDate();
	void setModifiedDate(const string &modifiedDate);

	const char *getModifiedBy();
	void setModifiedBy(const string &modifiedBy);

protected:

	string id;
	string name;
	string coverUrl;
	string detailsUrl;
	string ownerName;
	string ownerUrl;
	string ownerEmail;
	string status;
	string purpose;
	string type;
	string platform;
	string availability;
	string exclusion;
	string exclusionUserAgent;
	string noindex;
	string host;
	string from;
	string userAgent;
	string language;
	string description;
	string history;
	string environment;
	string modifiedDate;
	string modifiedBy;

};

#endif