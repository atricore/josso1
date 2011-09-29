package org.josso.liferay5.agent;

import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsUtil;

/**
 * Util for accessing JOSSO-specific Liferay configuration properties.
 *
 * @author <a href="mailto:dfisic@josso.org">Dusan Fisic</a>
 */
public class JossoLiferayProps {

    //third paramater in these functions is default value
    public static boolean isEnabled(long companyId) {
        try {
            return PrefsPropsUtil.getBoolean(companyId, JOSSSO_SSO_AUTH_ENABLED, GetterUtil.getBoolean(
                    PropsUtil.get(JOSSSO_SSO_AUTH_ENABLED)));
        } catch (SystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
    }

    public static final String JOSSSO_SSO_AUTH_ENABLED = "josso.sso.auth.enabled";
}