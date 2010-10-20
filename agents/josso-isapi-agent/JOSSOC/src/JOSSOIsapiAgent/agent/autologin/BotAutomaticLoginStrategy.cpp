#include <JOSSOIsapiAgent/agent/autologin/BotAutomaticLoginStrategy.hpp>
#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <fstream>
#include <crtdbg.h>
#include <algorithm>

#include "JOSSOIsapiAgent/agent/SSOAgentRequest.hpp"
#include "JOSSOIsapiAgent/agent/SSOAgentResponse.hpp"
#include "JOSSOIsapiAgent/util/StringUtil.hpp"

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

const char * BotAutomaticLoginStrategy::getBotsFile() {
	return this->botsFile.c_str();
}

void BotAutomaticLoginStrategy::setBotsFile(const string &botsFile) {
	this->botsFile.assign(botsFile);
}

/**
 * This will not require an automatic login when a bot is crawling the site.
 */
bool BotAutomaticLoginStrategy::isAutomaticLoginRequired(SSOAgentRequest *req, SSOAgentResponse *res) {
	if (bots.empty()) {
		loadRobots();
	}
	string userAgent = req->getHeader("HTTP_USER_AGENT");
	if (bots.find(userAgent) != this->bots.end()) {
		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Autologin not required for bot: %s", userAgent.c_str());
		return false;
	}
	return true;
}

/**
 * Loads bots from the file.
 */
void BotAutomaticLoginStrategy::loadRobots() {
	ifstream file;
	file.open(botsFile.c_str(), ifstream::in);
	if (file.is_open()) {
		Robot *robot = new Robot();
		string line;
		string name = "";
		string value = "";
		while (!file.eof()) {
			getline(file, line);
			StringUtil::trim(line);
			if (!line.empty()) {
				if (!line.find("robot-") || !line.find("modified-")) {
					size_t separatorIndex = line.find(":");
        			name = line.substr(0, separatorIndex);
					value = line.substr(separatorIndex + 1);
					StringUtil::trim(name);
					StringUtil::trim(value);
					value.erase(std::remove(value.begin(), value.end(), '\t'), value.end());
            		setRobotProperty(robot, name, value, false);
        		} else {
        			setRobotProperty(robot, name, line, true);
        		}
        	} else {
				string userAgent (robot->getUserAgent());
				if (!userAgent.empty()) {
					bots[userAgent] = robot;
				}
        		robot = new Robot();
				name.clear();
        		value.clear();
        	}
		}

		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Loaded bots file: %s", botsFile.c_str());

		file.close();
	} else {
		jk_log(ssoAgent->logger, JK_LOG_DEBUG, "Error opening bots file: %s", this->botsFile.c_str());
	}
}

/**
 * Sets robot property value.
 * 
 * @param robot robot
 * @param name property name
 * @param value property value
 * @param append true if value should be appended to existing value, false otherwise
 */
void BotAutomaticLoginStrategy::setRobotProperty(Robot *robot, string name, string value, bool append) {
	if (robot == NULL || name.empty() || value.empty()) {
		return;
	}
	
	StringUtil::trim(value);
	value.erase(std::remove(value.begin(), value.end( ), '\t'), value.end());

	if (!name.find("robot-id")) {
		robot->setId(value);
	} else if (!name.find("robot-name")) {
		robot->setName(value);
	} else if (!name.find("robot-cover-url")) {
		robot->setCoverUrl(value);
	} else if (!name.find("robot-details-url")) {
		robot->setDetailsUrl(value);
	} else if (!name.find("robot-owner-name")) {
		robot->setOwnerName(value);
	} else if (!name.find("robot-owner-url")) {
		robot->setOwnerUrl(value);
	} else if (!name.find("robot-owner-email")) {
		robot->setOwnerEmail(value);
	} else if (!name.find("robot-status")) {
		robot->setStatus(value);
	} else if (!name.find("robot-purpose")) {
		robot->setPurpose(value);
	} else if (!name.find("robot-type")) {
		robot->setType(value);
	} else if (!name.find("robot-platform")) {
		robot->setPlatform(value);
	} else if (!name.find("robot-availability")) {
		robot->setAvailability(value);
	} else if (!name.find("robot-exclusion-useragent")) {
		robot->setExclusionUserAgent(value);
	} else if (!name.find("robot-exclusion")) {
		robot->setExclusion(value);
	} else if (!name.find("robot-noindex")) {
		robot->setNoindex(value);
	} else if (!name.find("robot-host")) {
		robot->setHost(value);
	} else if (!name.find("robot-from")) {
		robot->setFrom(value);
	} else if (!name.find("robot-useragent")) {
		robot->setUserAgent(value);
	} else if (!name.find("robot-language")) {
		robot->setLanguage(value);
	} else if (!name.find("robot-description")) {
		string description (robot->getDescription());
		if (append && !description.empty()) {
			robot->setDescription(description + " " + value);
		} else {
			robot->setDescription(value);
		}
	} else if (!name.find("robot-history")) {
		string history (robot->getHistory());
		if (append && !history.empty()) {
			robot->setHistory(history + " " + value);
		} else {
			robot->setHistory(value);
		}
	} else if (!name.find("robot-environment")) {
		robot->setEnvironment(value);
	} else if (!name.find("modified-date")) {
		robot->setModifiedDate(value);
	} else if (!name.find("modified-by")) {
		robot->setModifiedBy(value);
	}
}