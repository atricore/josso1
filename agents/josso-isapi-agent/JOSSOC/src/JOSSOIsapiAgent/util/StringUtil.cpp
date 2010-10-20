#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <crtdbg.h>


#include "JOSSOIsapiAgent/util/StringUtil.hpp"

#include "JOSSOIsapiAgent/util/base64.h"
#include "JOSSOIsapiAgent/util/md5.h"

#ifdef _DEBUG
#define DEBUG_NEW new(_NORMAL_BLOCK, __FILE__, __LINE__)
#define new DEBUG_NEW
#endif


void StringUtil::tokenize(const string& str,
                      vector<string>& tokens,
                      const string& delimiters)
{
    // Skip delimiters at beginning.
    string::size_type lastPos = str.find_first_not_of(delimiters, 0);
    // Find first "non-delimiter".
    string::size_type pos     = str.find_first_of(delimiters, lastPos);

    while (string::npos != pos || string::npos != lastPos)
    {
        // Found a token, add it to the vector.
        tokens.push_back(str.substr(lastPos, pos - lastPos));
        // Skip delimiters.  Note the "not_of"
        lastPos = str.find_first_not_of(delimiters, pos);
        // Find next "non-delimiter"
        pos = str.find_first_of(delimiters, lastPos);
    }
}

void StringUtil::trim(string& str) {
	string::size_type pos = str.find_last_not_of(' ');
	if(pos != string::npos) {
		str.erase(pos + 1);
		pos = str.find_first_not_of(' ');

		if(pos != string::npos)  {
			str.erase(0, pos);
		}

	} else  {
		str.erase(str.begin(), str.end());
	}
}

string StringUtil::encode64(string str) {
	const string s = str.c_str();
	string encoded = base64_encode(reinterpret_cast<const unsigned char*>(s.c_str()), s.length());
	return encoded;

}

string StringUtil::decode64(string str) {

	string decoded = base64_decode(str);
	return decoded;

}

/*
 * creates a MD5 hash from
 * "text" and returns it as
 * string
 */	
std::string StringUtil::getHashFromString(std::string text)
{
	MD5 *md5;

	md5 = new MD5();

	MD5_CTX ctx;
	
	//init md5
	md5->MD5Init(&ctx);
	//update with our string
	md5->MD5Update(&ctx,
		 (unsigned char*)text.c_str(),
		 text.length());
	
	//create the hash
	unsigned char buff[16] = "";	
	md5->MD5Final((unsigned char*)buff,&ctx);

	delete md5;

	//converte the hash to a string and return it
	return convToString(buff);	
}


/*
 * converts the numeric hash to
 * a valid std::string.
 * (based on Jim Howard's code;
 * http://www.codeproject.com/cpp/cmd5.asp)
 */
std::string StringUtil::convToString(unsigned char *bytes)
{
	char asciihash[33];

	int p = 0;
	for(int i=0; i<16; i++)
	{
		::sprintf(&asciihash[p],"%02x",bytes[i]);
		p += 2;
	}	
	asciihash[32] = '\0';
	return std::string(asciihash);
}
