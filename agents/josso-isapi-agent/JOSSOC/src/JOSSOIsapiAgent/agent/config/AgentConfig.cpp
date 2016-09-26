#include <JOSSOISapiAgent/agent/config/AgentConfig.hpp>

const char * AgentConfig::getNodeId() {
	return nodeId.c_str();
}

void AgentConfig::setNodeId(const string &nodeId) {
	this->nodeId.assign(nodeId);
}

AgentConfig::AgentConfig() {

}