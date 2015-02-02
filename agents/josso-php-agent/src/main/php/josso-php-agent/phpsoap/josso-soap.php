
<?php

class ResolveAuthenticationAssertionRequestType{
    var $requester;//string
    var $assertionId;//string
}
class ResolveAuthenticationAssertionResponseType{
    var $ssoSessionId;//string
    var $securityDomain;//string
}
class AssertIdentityWithSimpleAuthenticationRequestType{
    var $requester;//string
    var $securityDomain;//string
    var $username;//string
    var $password;//string
}
class AssertIdentityWithSimpleAuthenticationResponseType{
    var $assertionId;//string
}
class GlobalSignoffRequestType{
    var $requester;//string
    var $ssoSessionId;//string
}
class GlobalSignoffResponseType{
    var $ssoSessionId;//string
}
class SSOIdentityProviderErrorType{
    var $errMessage;//string
}
class AssertionNotValidErrorType{
    var $assertionId;//string
}
class FindUserInSessionRequestType{
    var $requester;//string
    var $ssoSessionId;//string
}
class FindUserInSessionResponseType{
    var $SSOUser;//SSOUserType
}
class FindUserInSecurityDomainRequestType{
    var $requester;//string
    var $securityDomain;//string
    var $username;//string
}
class FindUserInSecurityDomainResponseType{
    var $SSOUser;//SSOUserType
}
class FindRolesBySSOSessionIdRequestType{
    var $requester;//string
    var $ssoSessionId;//string
}
class FindRolesBySSOSessionIdResponseType{
    var $username;//string
    var $roles;//SSORoleType
}
class UserExistsRequestType{
    var $requester;//string
    var $securityDomain;//string
    var $username;//string
}
class UserExistsResponseType{
    var $userexists;//boolean
}
class SSOIdentityManagerErrorType{
    var $errMessage;//string
}
class NoSuchUserErrorType{
    var $securityDomain;//string
    var $username;//string
}
class InvalidSessionErrorType{
    var $sessionId;//string
}
class AccessSessionRequestType{
    var $requester;//string
    var $ssoSessionId;//string
}
class AccessSessionResponseType{
    var $ssoSessionId;//string
}
class SessionRequestType{
    var $requester;//string
    var $sessionId;//string
}
class SessionResponseType{
    var $SSOSession;//SSOSessionType
}
class SSOSessionErrorType{
    var $errMessage;//string
}
class NoSuchSessionErrorType{
    var $sessionId;//string
}
class SSOSessionType{
    var $id;//string
    var $creationTime;//long
    var $lastAccessTime;//long
    var $maxInactiveInterval;//int
    var $username;//string
    var $accessCount;//long
    var $valid;//boolean
}
class SSOUserType{
    var $properties;//SSONameValuePairType
    var $name;//string
    var $securitydomain;//string
}
class SSORoleType{
    var $name;//string
}
class SSONameValuePairType{
    var $name;//string
    var $value;//string
}
class JOSSOSoapClient
{
    var $soapClient;

    private static $classmap = array('ResolveAuthenticationAssertionRequestType'=>'ResolveAuthenticationAssertionRequestType'
    ,'ResolveAuthenticationAssertionResponseType'=>'ResolveAuthenticationAssertionResponseType'
    ,'AssertIdentityWithSimpleAuthenticationRequestType'=>'AssertIdentityWithSimpleAuthenticationRequestType'
    ,'AssertIdentityWithSimpleAuthenticationResponseType'=>'AssertIdentityWithSimpleAuthenticationResponseType'
    ,'GlobalSignoffRequestType'=>'GlobalSignoffRequestType'
    ,'GlobalSignoffResponseType'=>'GlobalSignoffResponseType'
    ,'SSOIdentityProviderErrorType'=>'SSOIdentityProviderErrorType'
    ,'AssertionNotValidErrorType'=>'AssertionNotValidErrorType'
    ,'FindUserInSessionRequestType'=>'FindUserInSessionRequestType'
    ,'FindUserInSessionResponseType'=>'FindUserInSessionResponseType'
    ,'FindUserInSecurityDomainRequestType'=>'FindUserInSecurityDomainRequestType'
    ,'FindUserInSecurityDomainResponseType'=>'FindUserInSecurityDomainResponseType'
    ,'FindRolesBySSOSessionIdRequestType'=>'FindRolesBySSOSessionIdRequestType'
    ,'FindRolesBySSOSessionIdResponseType'=>'FindRolesBySSOSessionIdResponseType'
    ,'UserExistsRequestType'=>'UserExistsRequestType'
    ,'UserExistsResponseType'=>'UserExistsResponseType'
    ,'SSOIdentityManagerErrorType'=>'SSOIdentityManagerErrorType'
    ,'NoSuchUserErrorType'=>'NoSuchUserErrorType'
    ,'InvalidSessionErrorType'=>'InvalidSessionErrorType'
    ,'AccessSessionRequestType'=>'AccessSessionRequestType'
    ,'AccessSessionResponseType'=>'AccessSessionResponseType'
    ,'SessionRequestType'=>'SessionRequestType'
    ,'SessionResponseType'=>'SessionResponseType'
    ,'SSOSessionErrorType'=>'SSOSessionErrorType'
    ,'NoSuchSessionErrorType'=>'NoSuchSessionErrorType'
    ,'SSOSessionType'=>'SSOSessionType'
    ,'SSOUserType'=>'SSOUserType'
    ,'SSORoleType'=>'SSORoleType'
    ,'SSONameValuePairType'=>'SSONameValuePairType'

    );

    function __construct($url='http://www.josso.org/wsdl/josso-1.2/josso-1.2.wsdl')
    {
        $this->soapClient = new nusoap_client($url,array("classmap"=>self::$classmap,"trace" => true,"exceptions" => true));
    }

    function resolveAuthenticationAssertion($ResolveAuthenticationAssertionRequestType)
    {

        $ResolveAuthenticationAssertionResponseType = $this->soapClient->resolveAuthenticationAssertion($ResolveAuthenticationAssertionRequestType);
        return $ResolveAuthenticationAssertionResponseType;

    }
    function assertIdentityWithSimpleAuthentication($AssertIdentityWithSimpleAuthenticationRequestType)
    {

        $AssertIdentityWithSimpleAuthenticationResponseType = $this->soapClient->assertIdentityWithSimpleAuthentication($AssertIdentityWithSimpleAuthenticationRequestType);
        return $AssertIdentityWithSimpleAuthenticationResponseType;

    }
    function globalSignoff($GlobalSignoffRequestType)
    {

        $GlobalSignoffResponseType = $this->soapClient->globalSignoff($GlobalSignoffRequestType);
        return $GlobalSignoffResponseType;

    }
    function findUserInSession($FindUserInSessionRequestType)
    {

        $FindUserInSessionResponseType = $this->soapClient->findUserInSession($FindUserInSessionRequestType);
        return $FindUserInSessionResponseType;

    }
    function findUserInSecurityDomain($FindUserInSecurityDomainRequestType)
    {

        $FindUserInSecurityDomainResponseType = $this->soapClient->findUserInSecurityDomain($FindUserInSecurityDomainRequestType);
        return $FindUserInSecurityDomainResponseType;

    }
    function findRolesBySSOSessionId($FindRolesBySSOSessionIdRequestType)
    {

        $FindRolesBySSOSessionIdResponseType = $this->soapClient->findRolesBySSOSessionId($FindRolesBySSOSessionIdRequestType);
        return $FindRolesBySSOSessionIdResponseType;

    }
    function userExists($UserExistsRequestType)
    {

        $UserExistsResponseType = $this->soapClient->userExists($UserExistsRequestType);
        return $UserExistsResponseType;

    }
    function accessSession($AccessSessionRequestType)
    {

        $AccessSessionResponseType = $this->soapClient->accessSession($AccessSessionRequestType);
        return $AccessSessionResponseType;

    }
    function getSession($SessionRequestType)
    {

        $SessionResponseType = $this->soapClient->getSession($SessionRequestType);
        return $SessionResponseType;

    }}


?>