#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>


#include <JOSSOIsapiAgent/agent/SSOAgentResponse.hpp>

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif

const char* SSOAgentResponse::HTML_ERROR_HEAD =    "<!--\n"
                                "  JOSSO: Java Open Single Sign-On\n"
                                "Copyright 2004-2009, AtricoreTM, Inc.\n"
                                "\n"
                                "This is free software; you can redistribute it and/or modify it\n"
                                "under the terms of the GNU Lesser General Public License as\n"
                                "published by the Free Software Foundation; either version 2.1 of\n"
                                "the License, or (at your option) any later version.\n"
                                "\n"
                                "This software is distributed in the hope that it will be useful,\n"
                                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                                "GNU LESSER GENERAL PUBLIC LICENSE for more details.\n"
                                "  -->\n"
                                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"\n"
                                "\"http://www.w3c.org/TR/REC-html40/loose.dtd\">\n"
                                "<HTML>\n<HEAD>\n"
                                "<META http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">\n"
                                "<STYLE TYPE=\"text/css\">\n"
                                "    body {\n"
                                "       color: #000000;\n"
                                "       background-color: #FFFFFF;\n"
                                "       font-family: Verdana, Tahoma, Arial, Helvetica, sans-serif;\n"
                                "       font-size: 9pt;\n"
                                "       margin: 10px 10px;\n"
                                "    }\n"
                                "    p#footer {\n"
                                "       text-align: right;\n"
                                "       font-size: 80%;\n"
                                "    }\n"
                                "</STYLE>\n";

const char* SSOAgentResponse::HTML_ERROR_BODY_FMT =      "<TITLE>%s!</TITLE>\n</HEAD>\n<BODY>\n<H1>%s!</H1>\n<P>\n%s\n</P>\n";


const char* SSOAgentResponse::HTML_ERROR_TAIL = "<P>\n<BR/>&nbsp;<BR/>&nbsp;<BR/>&nbsp;<BR/>&nbsp;\n"
                                VERSION_STRING "\n"
                                "<BR/>&nbsp;\n"
                                "<HR/>\n"
                                "<P id=\"footer\">\n"
                                "Copyright &copy; 1999-2009 Atricore, Inc.<BR/>\n"
                                "All Rights Reserved\n"
                                "</P>\n</BODY>\n</HTML>\n";

	


SSOAgentResponse::~SSOAgentResponse() {

}

bool SSOAgentResponse::sendRedirect(string url) {

	if (!addHeader("Content-Type", "text/html")) return false;
	if (!addHeader("Cache-Control", "no-cache")) return false;
	if (!addHeader("Pragma", "no-cache")) return false;
	if (!addHeader("Location", url)) return false;
	if (!addHeader("Expires", "0")) return false;

	if (!startResponse(HTTP_STATUS_REDIRECT, "Moved Temporarily", headers))
		return false;

	return true;
}

bool:: SSOAgentResponse::sendStatus(int status, string reason) {

	bool ok = true;

	addHeader("Content-Length", "0");
	addHeader("Content-Type", "text/html");
	
	ok = startResponse(status, reason, headers);

	return ok;

	/*
	char statusLine[MAX_HEADER_SIZE];
	statusLine[0] = '\0';
	StringCbPrintf(statusLine, MAX_HEADER_SIZE, "%d %s", status, reason.c_str());
	jk_log(logger, JK_LOG_TRACE, "Status line %s", statusLine);

	string content = this->HTML_ERROR_HEAD;

	char body[65534];
	body[0] = '\0';
	// TODO : Use error description!
	StringCbPrintf(body, 65534, this->HTML_ERROR_BODY_FMT, statusLine, statusLine, reason.c_str());
	

	string c = this->HTML_ERROR_HEAD;
	c.append(body);
	c.append(this->HTML_ERROR_TAIL);

	body[0] = '\0';

	StringCbCopy(body, c.size(), c.c_str());
	jk_log(logger, JK_LOG_TRACE, "Content \r\n%s", body);

	return  writeContent(body, c.size()); */
}

bool SSOAgentResponse::addHeader(string name, string value) {

	if (isCommitted()) {
		jk_log(logger, JK_LOG_WARNING, "Attempting to add a HTTP Header to a committed response (%s: %s)", name.c_str(), value.c_str());
		return false;
	}

	pair<string, string> header = pair<string, string>(name, value);
	headers.push_back(header);

	return true;

}

bool SSOAgentResponse::setCookie(string name, string value, string path) {
	return setCookie(name, value, path, false);
}

bool SSOAgentResponse::setCookie(string name, string value, string path, bool secure) {
	string cookie = name;

	cookie.append("=");
	cookie.append(value);
	
	cookie.append(";path=");
	cookie.append(path);
	cookie.append(";");

	/*
	cookie.append("max-age=");
	cookie.append("0");
	cookie.append(";");
    */
	if(secure){
		cookie.append("Secure;");
	}

	return addHeader("Set-Cookie", cookie);

}
