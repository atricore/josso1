#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <string>
#include <sstream>
#include <crtdbg.h>


#include <JOSSOIsapiAgent/agent/SSOAgentRequest.hpp>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif



const string SSOAgentRequest::EMPTY_PARAM = "__EMPTY_PARAM__";

string SSOAgentRequest::getPath() {

	if (uri.empty())
		uri = getServerVariable("HTTP_URL", INTERNET_MAX_URL_LENGTH);

	return uri;
}

string SSOAgentRequest::getCookie(string name) {

	if (cookies.empty()) {
		string cookieHeader = getHeader("HTTP_COOKIE");
		
		jk_log(logger, JK_LOG_TRACE, "Loading cookies from ", cookieHeader.c_str());
		vector <string> cs;
		StringUtil::tokenize(cookieHeader, cs, ";");

		vector<string>::iterator c;
		for (c = cs.begin() ; c != cs.end() ; c++) {
			string cookie = *c;
			// split each cookie
			size_t eqPos = cookie.find("=");
			if (eqPos >= 0) {
				string value = cookie.substr(eqPos+1);
				string name = cookie.substr(0, eqPos);
				StringUtil::trim(name);
				
				cookies[name] = value;

				jk_log(logger, JK_LOG_TRACE, "[%s]=[%s]", name.c_str(), value.c_str());
			}
		}
	}

	return cookies[name];

}


string SSOAgentRequest::getHeader(string name) {
	return getServerVariable(name, MAX_HEADER_SIZE);
}

/**
 * Return paramter value for the given name.  If returned string is empty, it means that the parameter was not present.
 * If the returned string is SSOAgentRequest::EMPTY_PARAM, it means that the parameter is present, without a value.
 */
string SSOAgentRequest::getParameter(string name) {

	string qryStr = this->getQueryString();

	if (params.empty() && !qryStr.empty()) {
		parseQueryString(qryStr);
		
		map<string, string>::iterator i;
		for (i = params.begin() ; i != params.end() ; i++) {
			pair <string, string> p = *i;
			jk_log(logger, JK_LOG_DEBUG, "Populating request with param %s=[%s]", p.first.c_str(), p.second.c_str());
		}

	}

	return params[name];
}

bool SSOAgentRequest::isUserInRole(string roleName) {
	return this->secCtx.hasRole(roleName);
}

bool SSOAgentRequest::isAuthenticated() {
	return this->secCtx.isAuthenticated();
}

string SSOAgentRequest::getRemoteUser() {
	return this->secCtx.getPrincipal();
}

bool SSOAgentRequest::parseQueryString(string qryStr) {
	vector<string> vParams;
	StringUtil::tokenize(qryStr, vParams, "&");

	vector<string>::iterator i;
	for (i = vParams.begin() ; i != vParams.end() ; i++) {
		string p = *i;
		string name, value;

		size_t pos = p.find("=");
		if (pos != string::npos) {
			// There is a '=', split the string
			name.assign(p.substr(0, pos));
			value.assign(p.substr(pos + 1));

		} else {
			// There is no '=', the param has no value
			name.assign(p);
			value.assign(SSOAgentRequest::EMPTY_PARAM);
		}
		jk_log(logger, JK_LOG_TRACE, "Received param %s:%s", name.c_str(), value.c_str());
		params[name] = value;
		
	}

	return true;
}

std::string SSOAgentRequest::URLdecode(const std::string& l)
{
	std::ostringstream L;
	for(std::string::size_type x=0;x<l.size();++x)
		switch(l[x])
	{
		case('+'):
			{
				L<<' ';
				break;
			}
		case('%'): // Convert all %xy hex codes into ASCII characters.
			{
				const std::string hexstr(l.substr(x+1,2)); // xy part of %xy.
				x+=2; // Skip over hex.
				if(hexstr=="26" || hexstr=="3D")
					// Do not alter URL delimeters.
					L<<'%'<<hexstr;
				else
				{
					std::istringstream hexstream(hexstr);
					int hexint;
					hexstream>>std::hex>>hexint;
					L<<static_cast<char>(hexint);
				}
				break;
			}
		default: // Copy anything else.
			{
				L<<l[x];
				break;
			}
	}
	return L.str();
}

