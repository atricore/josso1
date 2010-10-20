package org.josso.tooling.gshell.install.util;

import name.fraser.neil.plaintext.diff_match_patch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 */
public class DiffMatchPatchUtil {

    private static final Log log = LogFactory.getLog(DiffMatchPatchUtil.class);

    private static DiffMatchPatchUtil dMpUtil;
    private diff_match_patch dmp;

    private DiffMatchPatchUtil() {
        dmp = new diff_match_patch();
    }

    public static DiffMatchPatchUtil getInstance() {
        if (dMpUtil == null)
            dMpUtil = new DiffMatchPatchUtil();
        return dMpUtil;
    }

    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /*
    private String[] filesForDiff = {
            "/index.php",
            "/includes/functions.php",
            "/includes/functions_user.php",
            "/language/en/common.php",
            "/styles/prosilver/template/index_body.html",
            "/styles/prosilver/template/login_body.html"
    };
    */


    public void createPatchesForDirectories(String target_dir, String source_dir, String[] filesForDiff, String outPatchFile) {
        int count = 0;
        StringBuffer sbResult = new StringBuffer();
        File filePatch = new File(outPatchFile);

        try {
            for (String fStr : filesForDiff) {
                File targetFile = new File(target_dir + fStr);
                File sourceFile = new File(source_dir + fStr);
                count++;

                String content_target = FileUtils.readFileToString(targetFile, null);
                String content_source = FileUtils.readFileToString(sourceFile, null);

                if (content_target != null && content_source != null) {
                    // An array of differences is computed which describe the 
                    // transformation of content_source into content_target.
                    LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(content_source, content_target);
                    LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diffs);
                    sbResult.append("--------------------------------------------------\n");
                    sbResult.append("Patch " + count + " | (" + fStr + ")\n");
                    sbResult.append("--------------------------------------------------\n");
                    sbResult.append(dmp.patch_toText(patches) + "\n");
                }
            }
            FileUtils.writeStringToFile(filePatch, sbResult.toString());

        } catch (FileNotFoundException e) {
            log.debug(e.getMessage());
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }

    /*
    Creates hashmap with <FilePath, Patches>
     */

    public HashMap<String, List<diff_match_patch.Patch>> getPatchesFromFile(String fileName) {
        HashMap<String, List<diff_match_patch.Patch>> resultMap = new HashMap<String, List<diff_match_patch.Patch>>();
        File srcFile = new File(fileName);
        try {
            String patchFileStr = FileUtils.readFileToString(srcFile, null);
            String REGEX = "--------------------------------------------------\n";
            Pattern p = Pattern.compile(REGEX);
            String[] items = p.split(patchFileStr, 0);
            String fileN = "";

            for (int i = 0; i < items.length; i++) {
                if (items[i].contains("Patch")) {
                    fileN = items[i].substring(items[i].indexOf("(") + 1, items[i].indexOf(")"));
                    String patch = items[i + 1];
                    List<diff_match_patch.Patch> patches = dmp.patch_fromText(patch);
                    if (!"".equals(fileN) && patches != null) {
                        resultMap.put(fileN, patches);
                        fileN = "";
                    }
                }
            }

        } catch (IOException e) {
            log.debug(e.getMessage());
        }

        return resultMap;
    }

    /*
    Applies patches for one file
    */

    public boolean applyPatch(String fileName, List<diff_match_patch.Patch> patches) {
        File targetFile = new File(fileName);
        LinkedList llPatches = new LinkedList<diff_match_patch.Patch>(patches);
        boolean statusOk = true;

        try {
            String patchFileStr = FileUtils.readFileToString(targetFile, null);
            Object[] resultArr = dmp.patch_apply(llPatches, patchFileStr);
            String patchedText = (String) resultArr[0];
            boolean[] pResults = (boolean[]) resultArr[1];
            for (int i = 0; i < pResults.length; i++) {
                statusOk = statusOk && pResults[i];
            }
            if (statusOk) {
                FileUtils.writeStringToFile(targetFile, patchedText);
            } else return false;
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
        return true;
    }

    public static void main(String[] args) {
        HashMap<String, List<diff_match_patch.Patch>> patchesMap = DiffMatchPatchUtil.getInstance().
                getPatchesFromFile("/home/fish/tmp/patch.file");
        String tgDir = "/home/fish/tmp/original/phpBB3";
        Iterator kIter = patchesMap.keySet().iterator();
        while (kIter.hasNext()) {
            String fName = (String) kIter.next();
            List<diff_match_patch.Patch> patches = patchesMap.get(fName);
            boolean statusOk = DiffMatchPatchUtil.getInstance().applyPatch(new String(tgDir+fName), patches);
            System.out.println("Filename: " + fName);
            //System.out.println("Patch: " + patches);
            System.out.println("Status: " + statusOk);
        }
    }
}
