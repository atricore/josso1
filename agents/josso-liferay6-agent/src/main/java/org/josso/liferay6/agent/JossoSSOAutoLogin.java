package org.josso.liferay6.agent;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.PwdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.service.SSOIdentityManagerService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Locale;

/**
 * Implementation of Liferay AutoLogin function and it's login method
 * Date: 14.05.2010.
 * Time: 19.25.41
 */

public class JossoSSOAutoLogin implements AutoLogin {

    private static final Log log = LogFactory.getLog(JossoSSOAutoLogin.class);


    public String[] login(HttpServletRequest request, HttpServletResponse response)
            throws AutoLoginException {

        String[] credentials = null;
        User user = null;

        try {
            long companyId = PortalUtil.getCompanyId(request);

            if (!JossoLiferayProps.isEnabled(companyId)) {
                return credentials;
            }

            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-agent-config.xml");
            HttpSSOAgent agent = (HttpSSOAgent) lookup.lookupSSOAgent();
            Cookie jCookie = getJossoCookie(request);
            if (jCookie == null || jCookie.getValue().equals("-")) {
                return credentials;
            }

            String jossoSessionId = jCookie.getValue();

            SSOIdentityManagerService im = Lookup.getInstance().lookupSSOAgent().getSSOIdentityManager();
            SSOUser ssoUser = im.findUserInSession(jossoSessionId, jossoSessionId);
            if (ssoUser == null) {
                return credentials;
            }

            //String screenName = request.getUserPrincipal().getName();
            String screenName = ssoUser.getName();

            String firstName = "";
            String lastName = "";
            String email = "";
            for (SSONameValuePair nameValuePair : ssoUser.getProperties()) {

                if (nameValuePair.getName().equals("user.name")) {
                    firstName = nameValuePair.getValue();

                } else if (nameValuePair.getName().equals("urn:org:atricore:idbus:user:property:firstName")) {
                    firstName = nameValuePair.getValue();

                } else if (nameValuePair.getName().equals("user.lastName")) {
                    lastName = nameValuePair.getValue();

                } else if (nameValuePair.getName().equals("urn:org:atricore:idbus:user:property:lastName")) {
                    lastName = nameValuePair.getValue();

                } else if (nameValuePair.getName().equals("email")) {
                    email = nameValuePair.getValue();

                } else if (nameValuePair.getName().equals("urn:org:atricore:idbus:user:property:email")) {
                    email = nameValuePair.getValue();
                }
            }

            try {
                user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
            } catch (NoSuchUserException nsue) {
                if (email == null || (email != null && email.length() == 0)) {
                    log.debug("Using user's screenName " + screenName + " as his email");
                    email = screenName;
                }

                try {
                    user = UserLocalServiceUtil.getUserByEmailAddress(companyId, email);
                } catch (Exception e) {

                }


                if (user == null) {

                    Locale locale = LocaleUtil.getDefault();

                    ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

                    if (themeDisplay != null) {

                        // ThemeDisplay should never be null, but some users
                        // complain of this error. Cause is unknown.

                        locale = themeDisplay.getLocale();
                    }


                    log.debug("Adding user : (companyId=" + companyId + ",firstName=" + firstName + ",lastName=" + lastName +
                    ",email=" + email + ",screeName=" + screenName + ",locale=" + locale + ")");

                    user = addUser(companyId, firstName, lastName, email, screenName, locale);
                }
            }

            credentials = new String[3];

            credentials[0] = String.valueOf(user.getUserId());
            credentials[1] = user.getPassword();
            credentials[2] = Boolean.TRUE.toString();
        } catch (Exception e) {
            log.error(e, e);
        }
        return credentials;
    }

    private User addUser(
            long companyId, String firstName, String lastName,
            String emailAddress, String screenName, Locale locale)
            throws Exception {

        long creatorUserId = 0;
        boolean autoPassword = false;
        String password1 = PwdGenerator.getPassword();
        String password2 = password1;
        //boolean autoScreenName = false; // force screen name autogeneration
        boolean autoScreenName = true;
        long facebookId = 0;
        String openId = StringPool.BLANK;
        String middleName = StringPool.BLANK;
        int prefixId = 0;
        int suffixId = 0;
        boolean male = true;
        int birthdayMonth = Calendar.JANUARY;
        int birthdayDay = 1;
        int birthdayYear = 1970;
        String jobTitle = StringPool.BLANK;
        long[] groupIds = null;
        long[] organizationIds = null;
        long[] roleIds = null;
        long[] userGroupIds = null;
        boolean sendEmail = false;
        ServiceContext serviceContext = new ServiceContext();

        return UserLocalServiceUtil.addUser(
                creatorUserId, companyId, autoPassword, password1, password2,
                autoScreenName, screenName, emailAddress, facebookId, openId, locale, firstName,
                middleName, lastName, prefixId, suffixId, male, birthdayMonth,
                birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds,
                roleIds, userGroupIds, sendEmail, serviceContext);

    }

    private Cookie getJossoCookie(HttpServletRequest hreq) {
        Cookie cookie = null;
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }

        return cookie;
    }
}
