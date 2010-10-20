package org.josso.gateway.identity.service.store.virtual.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.gateway.identity.service.store.virtual.UserMappingRule;
import org.josso.gateway.identity.service.store.virtual.BaseUserMappingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: MergeProperties.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="merge-properties"
 * <p/>
 * Create a set of virtual user properties by aggregating user properties from source user entries.
 */
public class MergeProperties extends BaseUserMappingRule implements UserMappingRule {

    private static final Log logger = LogFactory.getLog(MergeProperties.class);

    public BaseUser join(Collection<BaseUser> selectedUsers) {
        BaseUser jointUser;

        logger.debug("Joining properties from users " + selectedUsers);

        BaseUser firstUser = selectedUsers.iterator().next();
        jointUser = new BaseUserImpl(firstUser.getName());

        for (Iterator<BaseUser> baseUserIterator = selectedUsers.iterator(); baseUserIterator.hasNext();) {
            BaseUser baseUser = baseUserIterator.next();

            List<SSONameValuePair> properties = Arrays.asList(baseUser.getProperties());

            for (Iterator<SSONameValuePair> ssoNameValuePairIterator = properties.iterator(); ssoNameValuePairIterator.hasNext();) {
                SSONameValuePair ssoNameValuePair = ssoNameValuePairIterator.next();

                jointUser.addProperty(ssoNameValuePair);
            }
        }

        return jointUser;
    }

}
