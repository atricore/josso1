package org.josso.tooling.gshell.install.installer;

import name.fraser.neil.plaintext.diff_match_patch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.util.DiffMatchPatchUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 * @org.apache.xbean.XBean element="phpbb-installer"
 */

public class PhpBBInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(PhpBBInstaller.class);
    public static final String phpBBVersion = "3.0.";

    public PhpBBInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public PhpBBInstaller() {
        super();
    }

    public void validatePlatform() throws InstallException {

        try {
            boolean valid = true;
            FileObject includesDir = targetDir.resolveFile("includes/");

            if (!includesDir.exists() && !includesDir.getType().getName().equals(FileType.FOLDER.getName()) &&
                    !includesDir.resolveFile("constants.php").exists()) {
                FileContent constContent = includesDir.resolveFile("constants.php").getContent();
                BufferedReader inStream = new BufferedReader(new InputStreamReader(constContent.getInputStream()));
                String line = "";
                while ((line = inStream.readLine()) != null) {
                    if (line.contains("PHPBB_VERSION")) break;
                }
                if (!line.contains(phpBBVersion))
                    valid = false;
                log.debug("Found phpBB version : " + line.substring(line.lastIndexOf("PHPBB_VERSION")));
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " version " + phpBBVersion);

        } catch (IOException e) {
            getPrinter().printErrStatus("phpBB root", e.getMessage());
            throw new InstallException(e.getMessage(), e);
        }

        getPrinter().printOkStatus("phpBB root");
    }

    public void installConfiguration(JOSSOArtifact artifact, boolean replace)
            throws InstallException {
        log.debug("[phpBBInstall]: phpBB install dir: " + targetDir);

        try {
            FileObject authDir = targetDir.resolveFile("includes/auth/");
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            String name = srcFile.getName().getBaseName();

            if (name.startsWith("auth_josso")) {
                installFile(srcFile, authDir, replace);
            } else if (name.startsWith("patch")) { //install patches
                HashMap<String, List<diff_match_patch.Patch>> patchesMap = DiffMatchPatchUtil.getInstance().
                        getPatchesFromFile(srcFile.getURL().getPath());
                Iterator kIter = patchesMap.keySet().iterator();
                while (kIter.hasNext()) {
                    String fName = (String) kIter.next();
                    List<diff_match_patch.Patch> patches = patchesMap.get(fName);
                    String fileName = getLocalFilePath(targetDir) + fName;
                    FileObject bkpFile = getFileSystemManager().resolveFile(fileName);
                    backupFile(bkpFile, bkpFile.getParent());  //backup before you go

                    boolean statusOk = DiffMatchPatchUtil.getInstance().applyPatch(fileName, patches);
                    if (!statusOk) {
                        getPrinter().printActionErrStatus("Configure", fName, "Failed to patch file: " + fileName + ". Must be done manually.");
                        log.debug("[phpBBInstall]: Failed to patch file: " + fileName);
                    } else {
                        getPrinter().printActionOkStatus("Configure", fName, "Patching file: " + fileName);
                        log.debug("[phpBBInstall]: Patched file: " + fileName);
                    }
                }
            } else {
                installFile(srcFile, targetDir, replace);
            }

        } catch (FileSystemException e) {
            getPrinter().printActionErrStatus("Configure", "phpBB", "phpBB install directory is wrong.");
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
    }

    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
    }

    public boolean backupAgentConfigurations(boolean remove) {
        return true;
    }

    public boolean removeOldComponents(boolean backup) {
        return true;
    }

    public void configureAgent() throws InstallException {
    }
}