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

const string SSOAgentRequest::EMPTY_STR = "";

string SSOAgentRequest::getPath() {

	if (uri.empty())
		uri = getServerVariable("HTTP_URL", INTERNET_MAX_URL_LENGTH);

	return uri;
}

string SSOAgentRequest::getCookie(string cName) {

	jk_log(logger, JK_LOG_TRACE, "Requesting COOKIE [%s]", cName.c_str());

	if (cookies.empty()) {
		string cookieHeader = getHeader("HTTP_COOKIE");
		
		jk_log(logger, JK_LOG_TRACE, "Loading cookies from HTTP_COOKIE:\n%s", cookieHeader.c_str());
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

				jk_log(logger, JK_LOG_TRACE, "Storing COOKIE [%s]=[%s]", name.c_str(), value.c_str());
				
				cookies[name] = value;
			
			}
		}
	}

	map<string, string>::const_iterator cookies_it;
	cookies_it = cookies.find(cName);

	if (cookies_it == cookies.end()) {
		jk_log(logger, JK_LOG_TRACE, "Requested COOKIE %s=[<EMPTY>]", cName.c_str());
		return SSOAgentRequest::EMPTY_STR;
	} else {
		jk_log(logger, JK_LOG_TRACE, "Requested COOKIE %s=[%s]", cName.c_str(), cookies_it->second.c_str());
		return cookies_it->second;
	}

}


string SSOAgentRequest::getHeader(string name) {
	return getServerVariable(name, MAX_HEADER_SIZE);
}

/**
 * Return paramter value for the given name.  If returned string is empty, it means that the parameter was not present.
 * If the returned string is SSOAgentRequest::EMPTY_PARAM, it means that the parameter is present, without a value.
 */
string SSOAgentRequest::getParameter(string pName) {

	if (params.empty()) {

		string qryStr = this->getQueryString();
		if (!qryStr.empty()) {
			// Read and parse query string into parameters.
			parseQueryString(qryStr);
			
		} else if (!strcmp(getMethod().c_str(), "POST")) {
			
			// Read and parse request body into parametsers.
			DWORD bodySz = this->getBodySize();
			
			if (bodySz > 0) {
				LPBYTE body = this->getBody();
				jk_log(logger, JK_LOG_DEBUG, "POST Request has a body, type=[%s] size=%d", getContentType().c_str(), bodySz);
				parsePostData(body, bodySz, this->getContentType());
			} else {
				jk_log(logger, JK_LOG_DEBUG, "POST Request has no body");
			}
			
		} else {
			jk_log(logger, JK_LOG_TRACE, "GET Request has no parameters, method [%s]", getMethod().c_str());
		}

		// Just for debugging purposes
		map<string, string>::iterator i;
		for (i = params.begin() ; i != params.end() ; i++) {
			pair <string, string> p = *i;
			jk_log(logger, JK_LOG_DEBUG, "Populating request with param %s=[%s]", p.first.c_str(), p.second.c_str());
		}
	} 

	map<string, string>::const_iterator params_it;
	params_it = params.find(pName);

	// Did we find the parameter ?!
	if (params_it == params.end()) {
		return SSOAgentRequest::EMPTY_STR;
	} else {
		return params_it->second;
	}
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

bool SSOAgentRequest::parsePostData(LPBYTE body, DWORD bodySize, string contentType) {
	jk_log(logger, JK_LOG_TRACE, "Parsing [%d] bytes of type %s as parameters", bodySize, contentType.c_str());
	char * b = (char*) body;

	string sBody;
	sBody.assign(b, bodySize);

	if (!strcmp(contentType.c_str(), "application/x-www-form-urlencoded"))
		jk_log(logger, JK_LOG_WARNING, "POST Data content type unknown : [%s]", contentType.c_str());

	// TODO : This only works if post data is encoded as a query string
	jk_log(logger, JK_LOG_TRACE, "Body type[%s]\n%s", contentType.c_str(), sBody.c_str());

	return parseQueryString(sBody);
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

