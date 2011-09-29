package org.josso.atlassian.seraph;

import com.atlassian.crowd.embedded.api.Directory;
import org.josso.gateway.identity.SSOUser;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public interface CrowdDirectorySelectorStrategy {

    Directory lookupDirectory(SSOUser user);

}
