#ifndef SSO_USER_DEF
#define SSO_USER_DEF

class SSOUser {

public:
	string getUsername() { return username; }

private:
	string username;
	set <string> roles;
}

#endif