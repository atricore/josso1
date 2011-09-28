package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import org.apache.log4j.Logger;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;

import java.util.*;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public class DnBasedCrowdDirectorySelector extends AbstractCrowdDirectorySelectorStrategy {

    private static final Logger logger = Logger.getLogger(DnBasedCrowdDirectorySelector.class);

    private String dnUserProperty;

    private Map<String, String> dirsByDn = new HashMap<String, String>();

    public DnBasedCrowdDirectorySelector(Map<String, String> initParams, CrowdDirectoryService directoryService) {
        super(initParams, directoryService);
        String str = initParams.get("directory.dn.map");
        dnUserProperty = initParams.get("user.dn.property");
        if (dnUserProperty == null)
            dnUserProperty = "josso_user_dn";

        StringTokenizer st = new StringTokenizer(str, ";", false);

        while (st.hasMoreTokens()) {
            String nvPair = st.nextToken();

            int split = nvPair.indexOf(':');

            String dn = nvPair.substring(0, split);
            String dirName = nvPair.substring(split + 1);

            logger.info("Configuring base DN ["+dn+"] for Directory ["+dirName+"]");

            dirsByDn.put(dn, dirName);
        }

    }

    public Directory lookupDirectory(SSOUser user) {
        String directoryName = getDirectoryNameForUser(user);

        List<Directory> all = getDirectoryService().findAllDirectories();

        for (int i = 0; i < all.size(); i++) {
            Directory directory = all.get(i);
            if (directoryName.equals(directory.getName())) {
                logger.debug("Crowd User Directory ["+directory.getId()+"/"+directory.getName()+"] found for ["+user.getName()+"]");
                return directory;
            }
        }

        logger.error("No Crowd User Directory found for ["+user.getName()+"]");

        return null;
    }

    protected String getDirectoryNameForUser(SSOUser user) {
        SSONameValuePair[] props = user.getProperties();
        String dn = null;
        for (SSONameValuePair prop : props) {
            if (prop.getName().equalsIgnoreCase(dnUserProperty)) {
                dn = prop.getValue();
                break;
            }
        }

        if (dn == null) {
            logger.error("SSOUser does not have property " + dnUserProperty);
            return null;
        }

        // Match DN with dir name, i.e. dc=my-domain,dc=com:dirname;
        for (String baseDn : dirsByDn.keySet()) {

            logger.error("[" + dn + "] ["+baseDn+"]");
            if (dn.endsWith(baseDn)) {
                String dirName = dirsByDn.get(baseDn);
                logger.error("Found configured base DN ["+baseDn+"]for user ["+user.getName()+"], using directory ["+dirName+"]");
                return dirName;
            }
        }

        logger.error("No based DN found for user DN ["+dn+"]");

        return null;

    }
}
