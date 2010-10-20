#ifndef STRING_UTIL
#define STRING_UTIL

#include <vector>

using namespace std;

class StringUtil {

private:
		/*
		 * converts the numeric giets to
		 * a valid std::string
		 */
	static std::string convToString(unsigned char *bytes);


public:
	static void tokenize(const string& str,
                      vector<string>& tokens,
                      const string& delimiters = " ");

	static void trim(string &str);

	static string encode64(string str);

	static string decode64(string str);

	/*
	 * creates a MD5 hash from
	 * "text" and returns it as
	 * string
	 */	
	static std::string getHashFromString(std::string text);


};
#endif