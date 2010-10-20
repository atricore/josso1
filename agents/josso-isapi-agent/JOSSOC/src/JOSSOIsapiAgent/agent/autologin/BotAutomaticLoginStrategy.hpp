#ifndef BOT_AUTOMATIC_LOGIN_STRATEGY_DEF
#define BOT_AUTOMATIC_LOGIN_STRATEGY_DEF

#include <JOSSOIsapiAgent/agent/autologin/AbstractAutomaticLoginStrategy.hpp>
#include <JOSSOIsapiAgent/agent/autologin/Robot.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>
#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>

#include <string>

using namespace std;

class BotAutomaticLoginStrategy : public AbstractAutomaticLoginStrategy {
public:

	BotAutomaticLoginStrategy() {}

	BotAutomaticLoginStrategy(const string &mode) : AbstractAutomaticLoginStrategy(mode) {}

	const char * getBotsFile();

	void setBotsFile(const string &botsFile);

	bool isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res);

protected:
	
	string botsFile;

private:

	map<string, Robot*> bots;

	void loadRobots();

	void setRobotProperty(Robot *robot, string name, string value, bool append);
};

#endif