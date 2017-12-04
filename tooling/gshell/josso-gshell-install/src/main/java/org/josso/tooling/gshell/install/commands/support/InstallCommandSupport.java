/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.tooling.gshell.install.commands.support;

import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.branding.VersionLoader;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.josso.tooling.gshell.core.support.JOSSOCommandSupport;
import org.josso.tooling.gshell.install.*;
import org.josso.tooling.gshell.install.installer.InstallException;
import org.josso.tooling.gshell.install.installer.Installer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.Map;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 14, 2008
 * Time: 8:12:03 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class InstallCommandSupport extends JOSSOCommandSupport {

    @Requirement
    protected VersionLoader versionLoader;

    @Option(name = "-t", aliases = {"--target"}, description = "Target Install directory", required = true)
    private String target = "";

    /**
     * In the futur we could find the platform looking in the target directory
     */
    @Option(name = "-p", aliases = {"--platform"}, description = "see list-platforms for a complete list", required = true, argumentRequired = true)
    private String targetPlatformId = "";

    @Option(name = "-f", aliases = {"--force-install"}, description = "Force installation if some target validations fail", required = false, argumentRequired = false)
    private boolean forceInstall;

    @Option(name = "-r", aliases = {"--replace"}, description = "Replace installed files, includes configuration", required = false, argumentRequired = false)
    private boolean replaceConfig;

    @Option(name = "-idphn", aliases = {"--idp-host-name"}, description = "Define identity provider host name", required = false, argumentRequired = true)
    private String idpHostName = "localhost";

    @Option(name = "-idpp", aliases = {"--idp-port"}, description = "Define identity provider port", required = false, argumentRequired = true)
    private String idpPort = "8080";

    @Option(name = "-idpt", aliases = {"--idp-type"}, description = "Define identity provider type", required = false, argumentRequired = true)
    private String idpType = "josso";

    // Some platform specific properties

    @Option(name = "-i", aliases = {"--jboss-instance"}, description = "JBoss instance", required = false, argumentRequired = true)
    private String jbossInstance = "default";

    @Option(name = "-d", aliases = {"--weblogic-domain"}, description = "Weblogic domain path", required = false, argumentRequired = true)
    private String weblogicDomain = "samples/domains/wl_server";

    @Option(name = "-jdk", aliases = {"--target-jdk"}, description = "Agent target JDK", required = false, argumentRequired = true)
    private String targetJDK = null;

    @Option(name = "-u", aliases = {"--user"}, description = "Define user for server login", required = false, argumentRequired = false)
    private String user;

    @Option(name = "-w", aliases = {"--password"}, description = "Define password for server login", required = false, argumentRequired = false)
    private String password;

    @Option(name = "-td", aliases = {"--tcdir"}, description = "Define Tomcat install directory", required = false, argumentRequired = false)
    private String tomcatInstallDir;

    @Option(name = "-jd", aliases = {"--jbdir"}, description = "Define JBoss install directory", required = false, argumentRequired = false)
    private String jbossInstallDir;

    private Installer installer;

    /**
     * GShell uses the 'Prototype' pattern.  We want to make sure that we do not mix instances!
     *
     * @return
     * @throws Exception
     */
    @Override
    protected JOSSOCommandSupport createCommand() throws Exception {
        InstallCommandSupport newCommand = (InstallCommandSupport) super.createCommand();
        newCommand.setVersionLoader(versionLoader);
        return newCommand;
    }

    public String getTargetPlatformId() {
        return targetPlatformId;
    }

    public Installer getInstaller() {

        if (installer == null) {
            installer = createInstaller();
        }

        return installer;
    }

    protected Installer createInstaller() {
        try {

            log.debug("Creating installer for " + getTargetPlatformId());
            Map beans = getApplicationContext().getBeansOfType(Installer.class);
            Iterator it = beans.values().iterator();

            if (!(idpType.equals("josso") || idpType.equals("atricore-idbus"))) {
                throw new RuntimeException("idp-type should be 'josso' or 'atricore-idbus'");
            }

            try {
                Integer.valueOf(idpPort);
            } catch (NumberFormatException e) {
                throw new RuntimeException("idp-port should be a number");
            }
            if (Integer.valueOf(idpPort) < 1) {
                throw new RuntimeException("idp-port should be positive");
            }

            while (it.hasNext()) {
                Installer i = (Installer) it.next();
                if (i.getPlatformId().equals(getTargetPlatformId())) {

                    installer = i.createInstaller();

                    // TODO : Improve this!
                    if (jbossInstance != null) {
                        log.debug("Using 'jbossInstance' " + jbossInstance);
                        installer.setProperty("jbossInstance", jbossInstance);
                    }

                    if (weblogicDomain != null) {
                        log.debug("Using 'weblogicDomain' " + weblogicDomain);
                        installer.setProperty("weblogicDomain", weblogicDomain);
                    }

                    if (targetJDK != null) {
                        log.debug("Using 'targetJDK' " + targetJDK);
                        installer.setProperty("targetJDK", targetJDK);
                    }

                    if (user != null) {
                        log.debug("Using 'user' " + user);
                        installer.setProperty("user", user);
                    }

                    if (password != null) {
                        log.debug("Using 'password' " + password);
                        installer.setProperty("password", password);
                    }

                    if (target != null)
                        installer.setProperty("target", target);

                    if (versionLoader != null)
                        installer.setProperty("version", versionLoader.getVersion());

                    if (tomcatInstallDir != null) {
                        log.debug("Using 'tomcatInstallDir' " + tomcatInstallDir);
                        installer.setProperty("tomcatInstallDir", tomcatInstallDir);
                    }

                    if (jbossInstallDir != null) {
                        log.debug("Using 'jbossInstallDir' " + jbossInstallDir);
                        installer.setProperty("jbossInstallDir", jbossInstallDir);
                    }

                    return installer;
                }
            }

            throw new RuntimeException("No installer found for " + getTargetPlatformId() + " (see list-platforms command)");

        } catch (InstallException e) {
            throw new RuntimeException("Cannot create installer : " + e.getMessage(), e);
        }

    }

    public void setInstaller(Installer installer) {
        this.installer = installer;
    }

    public boolean isTargetPlatformIdValid() {
        try {
            getApplicationContext().getBean(targetPlatformId);
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }

        return true;
    }

    public VersionLoader getVersionLoader() {
        return versionLoader;
    }

    public void setVersionLoader(VersionLoader versionLoader) {
        this.versionLoader = versionLoader;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isForceInstall() {
        return forceInstall;
    }

    public boolean isReplaceConfig() {
        return replaceConfig;
    }

    protected String getHomeDir() {
        return System.getProperty("josso-gsh.home");
    }

    protected String getJOSSOVersion() {
        return this.versionLoader.getVersion();
    }

    public String getIdpHostName() {
        return idpHostName;
    }

    public String getIdpPort() {
        return idpPort;
    }

    public String getIdpType() {
        return idpType;
    }

    /**
     * @param artifactId
     * @param type
     * @return
     */
    protected JOSSOArtifact createAgentArtifact(String baseUrl, String artifactId, String classifier, String type) {
        return createArtifact(baseUrl, JOSSOScope.AGENT, artifactId, getJOSSOVersion(), classifier, type);
    }

    protected JOSSOArtifact createGatewayArtifact(String baseUrl, String artifactId, String classifier, String type) {
        return createArtifact(baseUrl, JOSSOScope.GATEWAY, artifactId, getJOSSOVersion(), classifier, type);
    }

    protected JOSSOArtifact createCustomGatewayArtifact(String baseUrl, String artifactId, String classifier, String type) {
        //version is empty, maven provider will extract that from baseUrl
        JOSSOArtifact customArtifact = createArtifact(baseUrl, JOSSOScope.GATEWAY, artifactId, "", classifier, type);
        customArtifact.setLocation(baseUrl);
        return customArtifact;
    }

    protected JOSSOArtifact createSampleArtifact(String baseUrl, String artifactId, String classifier, String type) {
        return createArtifact(baseUrl, JOSSOScope.SAMPLE, artifactId, getJOSSOVersion(), classifier,  type);
    }


    protected JOSSOArtifact createArtifact(String baseUrl, JOSSOScope scope, String finalName) {
        JOSSOArtifact artifact = new JOSSOArtifact(scope, baseUrl + (baseUrl.endsWith("/") ? "" : "/") + finalName);
        if (log.isDebugEnabled())
            log.debug("Created artifact representation : " + artifact.toString());
        return artifact;
    }

    protected JOSSOArtifact createArtifact(String baseUrl, JOSSOScope scope, String artifactId, String version, String classifier, String type) {
        JOSSOArtifact artifact = new JOSSOArtifact(artifactId, version, classifier, type, scope, baseUrl);
        if (log.isDebugEnabled())
            log.debug("Created artifact representation : " + artifact.toString());
        return artifact;
    }


}
