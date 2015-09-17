#ifndef ABSTRACT_SSO_AGENT_DEF
#define ABSTRACT_SSO_AGENT_DEF

#include <JOSSOIsapiAgent/isapi/josso_isapi.h>
#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>
#include <JOSSOIsapiAgent/agent/config/AgentConfig.hpp>
#include <JOSSOIsapiAgent/agent/config/EndpointConfig.hpp>

#include <list>

struct compareSecConstraintByPriority {
  bool operator()( const SecurityConstraintConfig first, const SecurityConstraintConfig second ) { 
	  return first.priority.compare(second.priority) <= 0 ;
  }
};


class AbstractAutomaticLoginStrategy;

// In the future, we can have a pure virtual base class : SSOAgent
class AbstractSSOAgent {
public:


	// Configuration properties:

	static const char *JOSSO_REGISTRY_LOCATION;

	static const char *JOSSO_LOG_FILE;

	static const char *JOSSO_LOG_LEVEL;

	static const char *JOSSO_AGENT_CONFIG;

	static const char *JOSSO_ERROR_PAGE;

	static const char *JOSSO_EXTENSION_URI;

	static const char *W3SVC_REGISTRY_KEY;

	static const long DEFAULT_SESSION_ACCESS_MIN_INTERVAL;
	
	static const long DEFAULT_CACHE_CLEANUP_MIN_INTERVAL;
	
	static const char *DEFAULT_SESSION_MANAGER_SERVICE_PATH;

	static const char *DEFAULT_IDENTITY_MANAGER_SERVICE_PATH;

	static const char *DEFAULT_IDENTITY_PROVIDER_SERVICE_PATH;

	static const char *JOSSO_DEFAULT_AUTH_LOGIN_STRATEGY;

	static const char *JOSSO_URLBASED_AUTH_LOGIN_STRATEGY;

	static const char *JOSSO_BOT_AUTH_LOGIN_STRATEGY;

	static const char *JOSSO_AUTH_LOGIN_REQUIRED;

	static const char *JOSSO_AUTH_LOGIN_SUFFICIENT;

	static const char *JOSSO_AUTH_LOGIN_OPTIONAL;

	static const long DEFAULT_SOAP_TRANSPORT_TIMEOUT;

	// JK Logger 
	static jk_logger_t *logger;

	/**
	 * Default constructor
	 */
	AbstractSSOAgent() { started = false; }

	/**
	 * JOSSO Gateway login URL
	 */
	char *getGwyLoginUrl() { return this->agentConfig->getGatewayLoginUrl(); }

	/**
	 * JOSSO Gateway logout URL
	 */
	char *getGwyLogoutUrl() { return this->agentConfig->getGatewayLogoutUrl(); }

	/**
	 * Agent base 'back_to' URL
	 */
	char *getBackToBaseUrl() { return this->agentConfig->getBackToBaseUrl(); }

	/**
	 * Session access min interval
	 */
	long getSessionAccessMinInterval() { return this->agentConfig->getSessionAccessMinInterval(); }
	
	/**
	 * Cache cleanup min interval
	 */
	long getCacheCleanupMinInterval() { return this->agentConfig->getCacheCleanupMinInterval(); }
	
	/**
	 * TRUE if agent was already started
	 */
	virtual bool isStarted() { return started; }

	/**
	 * This agent configuration object.
	 */
	virtual AgentConfig * getAgentConfig();

	/**
	 * Partner application configuration that patches the base-uri with the given path
	 */
	virtual PartnerAppConfig * getPartnerAppConfig(const string & host, const string & path) ;

	virtual PartnerAppConfig * getPartnerAppConfigById(string id) ;

	virtual PartnerAppConfig * getDefaultPartnerAppConfig() ;

	/**
	 * Endpoint configuration 
	 */
	virtual EndpointConfig * getEndpointConfig(string id);


	virtual string buildGwyLoginUrl(SSOAgentRequest *req) =0;


	// -------------------------------------------------
	// Lifecycle
	// -------------------------------------------------
	/**
	 * Start this agent, this will read agent configuration.
	 */
	virtual bool start();

	/**
	 * Stop this agent
	 */
	virtual bool stop();

	// -------------------------------------------------
	// Agent operations
	// -------------------------------------------------

	/**
	 * Resolve the given authentication assertion
	 */
	virtual bool resolveAssertion(string assertionId, string &ssoSessionId, SSOAgentRequest *req);

	/**
	 * creates the secuirity context associated with this request.
	 */
	virtual bool createSecurityContext(SSOAgentRequest *req, PartnerAppConfig * appCfg);

	/**
	 * Authenticats a user 
	 */
	// virtual void authenticate(SSOAgentRequest *req);

	/** 
	 * TRUE if the user associated to the request is authorized to access the web resource
	 */
	virtual bool isAuthorized(SSOAgentRequest *req) ;

	/** 
	 * TRUE if the request must be ignored
	 */
	virtual bool isIgnored(PartnerAppConfig * appCfg, SSOAgentRequest *req) ;


	virtual const char *getRequester(SSOAgentRequest *req) =0;

	bool match(const string &source, const string &regex);

	bool isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res);

	/** 
	 * The URI where the agent listens for SSO requests
	 */
	virtual char * getExtensionUri() =0;

protected:


	/**
	 * Find the user associated with the given session.
	 */
	virtual bool findUserInSession(string ssoSessionId, string &principal, map<string, string> & properties, SSOAgentRequest *ssoAgentReq);

	/**
	 * Find the user associated with the given session.
	 */
	virtual bool findRolesInSession(string ssoSessionId, vector<string> &roles, SSOAgentRequest *ssoAgentReq);


	/**
	 * Access SSO Session associated with the given request
	 */
	virtual bool accessSession(string ssoSessionId, SSOAgentRequest *ssoAgentReq);

	SecurityConstraintConfig *getSecurityConstraintConfig(const string & host, const string & path);

	virtual AgentConfig *createAgentConfig() =0 ;

	virtual bool configureAgent(AgentConfig *cfg);

	void logSoapFault(struct soap *soap);

	string getGatewayIdentityManagerServiceEndpoint(const string & nodeId);

	string getGatewayIdentityProviderServiceEndpoint(const string & nodeId);

	string getGatewaySessionManagerServiceEndpoint(const string & nodeId);

	AgentConfig *agentConfig;
	
	bool intervalPassed(FILETIME fromTime, long milliseconds);

	void removeUnusedCacheEntries();

	list<AbstractAutomaticLoginStrategy*> automaticStrategies;

private:
	bool started;

};

#endif