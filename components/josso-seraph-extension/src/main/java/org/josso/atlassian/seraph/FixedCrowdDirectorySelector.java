package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import org.apache.log4j.Logger;
import org.josso.gateway.identity.SSOUser;

import java.util.List;
import java.util.Map;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public class FixedCrowdDirectorySelector extends AbstractCrowdDirectorySelectorStrategy {

    private static final Logger logger = Logger.getLogger(FixedCrowdDirectorySelector.class);

    private long directoryId = 1;

    public FixedCrowdDirectorySelector(Map<String, String> initParams, CrowdDirectoryService directoryService) {
        super(initParams, directoryService);

        String idStr = getInitParam("directory.id");
        if (idStr == null)
            logger.warn("No configured directory id, using default" + directoryId);

        try {
            directoryId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid configured directory id format for ["+idStr+"], using default " + directoryId);
        }
    }


    public long getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(int directoryId) {
        this.directoryId = directoryId;
    }


    public Directory lookupDirectory(SSOUser user) {
        /*List<Directory> dirs = getDirectoryService().findAllDirectories();
        for (int i = 0; i < dirs.size(); i++) {
            Directory directory = dirs.get(i);
            logger.info(directory.getName());
        }*/

        return getDirectoryService().findDirectoryById(directoryId);
    }
}
