package org.josso.tooling.gshell.install.installer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @version $Id$
 * @org.apache.xbean.XBean element="iis-installer"
 */
public class IISInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(IISInstaller.class);

    public IISInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public IISInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {

        try {

            boolean valid = true;

            if (targetConfDir.exists() && !targetConfDir.getType().getName().equals(FileType.FOLDER.getName())
                    && targetLibDir.exists() && !targetLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                valid = false;
                getPrinter().printErrStatus("IIS_AGENT_DIR", "Cannot find IIS_AGENT_DIR directory.");
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("IIS_AGENT_DIR", e.getMessage());
            throw new InstallException(e.getMessage(), e);
        }

        getPrinter().printOkStatus("IIS_AGENT_DIR directory");
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            String locationStr = artifact.getLocation();

            if (!this.targetBinDir.exists())
                this.targetBinDir.createFolder();

            // Install only the proper artifact for the target platform ...
            if (artifact.getBaseName().startsWith("JOSSOIsapiAgent") &&
                    locationStr.contains("Win32") &&  locationStr.contains("Release") &&
                    this.getTargetPlatform().getId().equals("iis")) {
                installFile(srcFile, this.targetBinDir, replace);
            } else if (artifact.getBaseName().startsWith("JOSSOIsapiAgent") &&
                    locationStr.contains("Win64") &&  locationStr.contains("Release") &&
                    this.getTargetPlatform().getId().equals("iis64")) {
                installFile(srcFile, this.targetBinDir, replace);
            } else {
                log.debug("Artifact is not valid for selected platform : " + artifact);
            }
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        // do nothing - don't install 3rd Party libs
    }

    @Override
    public boolean backupAgentConfigurations(boolean remove) {
        try {
            // backup portal-ext.properties
            FileObject portalConfFile = targetConfDir.resolveFile("josso-agent-config.ini");
            if (portalConfFile.exists()) {
                // backup file in the same folder it is installed
                backupFile(portalConfFile, portalConfFile.getParent());
                if (remove) {
                    portalConfFile.delete();
                }
            }
        } catch (Exception e) {
            getPrinter().printErrStatus("BackupAgentConfigurations", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean removeOldComponents(boolean backup) {
        return true;
    }

    @Override
    public void installConfiguration(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            String name = srcFile.getName().getBaseName();

            if (!this.targetConfDir.exists())
                this.targetConfDir.createFolder();

            if (!this.targetJOSSOSharedLibDir.exists())
                this.targetJOSSOSharedLibDir.createFolder();

            if (name.startsWith("josso-agent-config")) {
                installFile(srcFile, this.targetConfDir, replace);
            }

            this.targetJOSSOSharedLibDir.createFolder();
            FileObject logFile = getFileSystemManager().resolveFile(this.targetJOSSOSharedLibDir.getName() + "/josso_isapi.log");
            logFile.createFile();
            FileObject regConfig = getFileSystemManager().resolveFile(this.targetConfDir.getName() + "/JOSSO-ISAPI-Config.reg");
            FileObject regEventLog = getFileSystemManager().resolveFile(this.targetConfDir.getName() + "/JOSSO-ISAPI-EventLog.reg");

            regConfig.createFile();
            fillRegConfigFile(regConfig);
            printInstallOkStatus(regConfig.getName().getBaseName(), "Created " + regConfig.getName().getFriendlyURI());

            regEventLog.createFile();
            fillRegEventLogFile(regEventLog);
            printInstallOkStatus(regEventLog.getName().getBaseName(), "Created " + regEventLog.getName().getFriendlyURI());

        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void configureAgent() throws InstallException {

    }

    private String toWindowsPath(FileObject fileObj) {
        String retStr = fileObj.getName().getURI();
        retStr = retStr.replace("file:///","");
        retStr = retStr.replaceAll("/", "\\\\\\\\");
        while (retStr.endsWith("\\")) {
            retStr = retStr.substring(0, retStr.length() - 1);
        }
        return retStr;
    }

    private void fillRegConfigFile(FileObject regConfig) throws InstallException {
        String outStr = "Windows Registry Editor Version 5.00\n" +
                "\n" +
                "[HKEY_LOCAL_MACHINE\\SOFTWARE\\Atricore]\n" +
                "\n" +
                "[HKEY_LOCAL_MACHINE\\SOFTWARE\\Atricore\\JOSSO Isapi Agent]\n" +
                "\n" +
                "[HKEY_LOCAL_MACHINE\\SOFTWARE\\Atricore\\JOSSO Isapi Agent\\1.8]\n" +
                "\"LogLevel\"=\"trace\"\n" +
                "\"ExtensionUri\"=\"/josso/JOSSOIsapiAgent.dll\"\n" +
                "\"LogFile\"=\""+ toWindowsPath(this.targetJOSSOSharedLibDir) +"\\\\josso_isapi.log\"\n" +
                "\"AgentConfigFile\"=\""+ toWindowsPath(this.targetConfDir) +"\\\\josso-agent-config.ini\"";

        OutputStreamWriter out = null;
        OutputStream fos;
        try {
            fos = regConfig.getContent().getOutputStream();
            out = new OutputStreamWriter(fos);

            out.write(outStr);
            out.flush();
        } catch (Exception e) {
            throw new InstallException(e.getMessage(), e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException ex) {
                throw new InstallException(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public boolean updateAgentConfiguration(String idpHostName, String idpPort, String idpType) {
        return false;
    }

    private void fillRegEventLogFile(FileObject regEventLog) throws InstallException {
        String outStr = "Windows Registry Editor Version 5.00\n" +
                "\n" +
                "[HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Eventlog\\Application\\JOSSO Isapi]\n" +
                "\"TypesSupported\"=dword:00000007\n" +
                "\"EventMessageFile\"=\"" + toWindowsPath(this.targetBinDir) + "\\\\JOSSOIsapiAgent.dll\"\n" +
                "\"CategoryCount\"=dword:00000007\n" +
                "\"CategoryMessageFile\"=\"" + toWindowsPath(this.targetBinDir) + "\\\\JOSSOIsapiAgent.dll\"";

        OutputStreamWriter out = null;
        OutputStream fos;
        try {
            fos = regEventLog.getContent().getOutputStream();
            out = new OutputStreamWriter(fos);

            out.write(outStr);
            out.flush();
        } catch (Exception e) {
            throw new InstallException(e.getMessage(), e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException ex) {
                throw new InstallException(ex.getMessage(), ex);
            }
        }
    }

}
