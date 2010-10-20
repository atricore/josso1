<%
' Copyright (c) 2005-2006, Novascope S.A. and the JOSSO team
' All rights reserved.
' Redistribution and use in source and binary forms, with or
' without modification, are permitted provided that the following
' conditions are met:
'
' * Redistributions of source code must retain the above copyright
'   notice, this list of conditions and the following disclaimer.
'
' * Redistributions in binary form must reproduce the above copyright
'   notice, this list of conditions and the following disclaimer in
'   the documentation and/or other materials provided with the
'   distribution.
'
' * Neither the name of the JOSSO team nor the names of its
'   contributors may be used to endorse or promote products derived
'   from this software without specific prior written permission.
'
' THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
' CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
' INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
' MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
' DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
' BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
' EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
' TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
' DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
' ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
' OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
' OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
' POSSIBILITY OF SUCH DAMAGE.

' --------------------------------------------------------------------------------------
' Access JOSSO Session to keep it alive ....
' --------------------------------------------------------------------------------------
dim jossoUserPresent
jossoUserPresent = accessJOSSOSession()

' =======================================================================================
'
' PUBLIC FUNCTIONS, To be used from user pages.
'
' =======================================================================================

' --------------------------------------------------------------------------------------
' Use this function when ever you want to start user authentication.
' --------------------------------------------------------------------------------------
function jossoRequestLogin()
    dim currentUrl
    currentUrl = jossoGetPageUrl()
    requestLoginForUrl(currentUrl)
end function

' --------------------------------------------------------------------------------------
' Use this function when ever you want to logout the current user.
' --------------------------------------------------------------------------------------
function jossoRequestLogout()
    ' TBD
end function

' --------------------------------------------------------------------------------------
' Creates a login url for the current page, use to create links to JOSSO login page
' --------------------------------------------------------------------------------------
function jossoCreateLoginUrl()

    dim loginUrl, currentUrl, baseCode

    baseCode = josso.getProperty("agent.basecode")
    loginUrl = josso.getProperty("gwy.login")
    currentUrl = jossoGetPageUrl()

    loginUrl = baseCode & "/josso-login.asp?josso_current_url=" & currentUrl

    jossoCreateLoginUrl = loginUrl

end function

' --------------------------------------------------------------------------------------
' Creates a logout url for the current page, use to create links to JOSSO logout page
' --------------------------------------------------------------------------------------
function jossoCreateLogoutUrl()

    dim logoutUrl, currentUrl, baseCode

    baseCode = josso.getProperty("agent.basecode")
    logoutUrl = josso.getProperty("gwy.logout")
    currentUrl = jossoGetPageUrl()

    logoutUrl = baseCode & "/josso-logout.asp?josso_current_url=" & currentUrl

    jossoCreateLogoutUrl = logoutUrl
end function

' --------------------------------------------------------------------------------------
' This function returns the current SSO user, if no user has been authenticated or if
' the SSO Session has expired, it returns null
' --------------------------------------------------------------------------------------
function jossoCurrentUser()
    dim ssoSession

    ' Get current SSO Session ID, if any ...
    ssoSession = getJOSSOToken()

    if ssoSession <> "" then

        ' We have a SSO Session, find the associated user ...
        ' If the session has expired, there will be no user associated to it.

        dim jUsr
        jUsr = josso.findUserInSession(ssoSession)

        ' Treat the jUsr as a string until we know we have an instance ...
        if jUsr <> "" then
            set jossoCurrentUser = josso.findUserInSession(ssoSession)
        else
            set jossoCurrentUser = nothing
        end if
    else
        set jossoCurrentUser = nothing
    end if

end function

' --------------------------------------------------------------------------------------
' This function returns true if the user is associated with the given role
' --------------------------------------------------------------------------------------
function isUserInRole(roleName)

    dim j, jossoRoles

    set jossoRoles = josso.getUserRoles(jossoSession)

    isUserInRole = false
    For j = 0 to ( jossoRoles.count() - 1 )
	    set jossoRole = jossoRoles.getRole(j)
	    if jossoRole.getName() = roleName then
	        isUserInRole = true
	        exit function
	    end if
	Next

end function

' --------------------------------------------------------------------------------------
' This fuction checks current JOSSO user, if no user is associated to this session, a
' redirect to the login page is performed.
' --------------------------------------------------------------------------------------
function checkJOSSOUser()
    dim jossoToken
    jossoToken = getJOSSOToken()

    ' Access session to check valid user ...
    If not josso.accessSession(jossoToken) Then
        requestLogin()
    End if
    ' User is authenticated, no need for login request ...

end function

' =======================================================================================
'
' PRIVATE FUNCTIONS AND UTILS, To be use by JOSSO ASP Agent
'
' =======================================================================================

' --------------------------------------------------------------------------------------
' This function allows public pages to access the SSO Session,
' usefull to keep-alive SSO session on public resources.
' --------------------------------------------------------------------------------------
function accessJOSSOSession()
    dim jossoToken
    jossoToken = getJOSSOToken()
    accessJOSSOSession = josso.accessSession(jossoToken)
end function

function requestLoginForUrl(currentUrl)
    dim baseCode, loginUrl, backToUrl
    dim host, uri

    host = Request.ServerVariables("server_name")

    ' Store original URL
    Session("josso.backToUrl") = currentUrl

    ' Recover JOSSO ASP Agent base code to build path info.
    baseCode = josso.getProperty("agent.basecode")

    If Request.ServerVariables("https") = "on" Then

        backToUrl = "https://" & host

        If Request.ServerVariables("server_port") <> 443 Then
            backToUrl = backToUrl & ":" & Request.ServerVariables("server_port")
        End if
    Else
        backToUrl = "http://" & host

        If Request.ServerVariables("server_port") <> 80 Then
            backToUrl = backToUrl & ":" & Request.ServerVariables("server_port")
        End if
    End If

    uri = backToUrl & Request.ServerVariables("path_info")

    backToUrl = backToUrl & baseCode &  "/josso-security-check.asp"

    loginUrl = josso.getProperty("gwy.login") & "?josso_back_to=" & backToUrl
    loginUrl = loginUrl & "&josso_partnerapp_host=" & host & "&josso_partnerapp_ctx=" & uri

    Response.Redirect(loginUrl)
end function


function requestLogoutForUrl(currentUrl)
    dim baseCode, logoutUrl, backToUrl

    backToUrl = currentUrl
    loginUrl = josso.getProperty("gwy.logout") & "?josso_back_to=" & backToUrl
    Response.Redirect(loginUrl)
    
end function


' --------------------------------------------------------------------------------------
' This function returns the JOSSO Token, used to acess josso methods
' --------------------------------------------------------------------------------------
function getJOSSOToken()
    ' Return JOSSO Token
    set getJOSSOToken = Request.Cookies("JOSSO_SESSIONID")
end function

' --------------------------
' Builds current page URL  :
' --------------------------
function jossoGetPageUrl()

    dim pageUrl

    ' Get protocol, server and port information
    If Request.ServerVariables("https") = "on" Then

        pageUrl = "https://" & Request.ServerVariables("server_name")

        If Request.ServerVariables("server_port") <> 443 Then
            pageUrl = pageUrl & ":" & Request.ServerVariables("server_port")
        End if
    Else
        pageUrl = "http://" & Request.ServerVariables("server_name")

        If Request.ServerVariables("server_port") <> 80 Then
            pageUrl = pageUrl & ":" & Request.ServerVariables("server_port")
        End if
    End If

    ' Get path info and query string
    pageUrl = pageUrl & Request.ServerVariables("path_info")

    If Request.QueryString <> "" Then
        pageUrl = pageUrl & "?" & Request.QueryString
    End if


    ' We have the current page URL.
    jossoGetPageUrl = pageUrl

end function


%>