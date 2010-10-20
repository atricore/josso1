package org.josso.gateway.identity.service.store.virtual.scripting;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Scripting Rule Engine based on the Bean Scripting Framework.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingRuleEngine.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class ScriptingRuleEngine {
    private static final Log logger = LogFactory.getLog(ScriptingRuleEngine.class);

    /**
     * Holds the "compiled" scripts and their information
     */
    protected static Map scripts = new Hashtable();

    public ScriptingRuleExecutionOutcome execute(String ruleScriptName, Collection<ScriptingRuleParameter> ruleParameters) throws Exception {

        BSFManager bsfManager = new BSFManager();
        ScriptingRuleExecutionOutcome outcome = new ScriptingRuleExecutionOutcomeImpl();

        String parsedScriptName = parseScriptName(ruleScriptName, bsfManager);

        Script script = loadScript(parsedScriptName);


        bsfManager.declareBean("outcome", outcome, ScriptingRuleExecutionOutcomeImpl.class);
        bsfManager.declareBean("log", logger, Log.class);

        for (Iterator<ScriptingRuleParameter> scriptingRuleParameterIterator = ruleParameters.iterator(); scriptingRuleParameterIterator.hasNext();) {
            ScriptingRuleParameter scriptingRuleParameter = scriptingRuleParameterIterator.next();

            bsfManager.declareBean(scriptingRuleParameter.getName(), scriptingRuleParameter.getValue(), scriptingRuleParameter.getType());

        }

        bsfManager.exec(script.lang, script.file.getCanonicalPath(), 0, 0,
                script.string);

        return outcome;
    }

    public ScriptingRuleExecutionOutcome evaluate(String ruleExpression, String language,
                                                  Collection<ScriptingRuleParameter> ruleParameters) throws Exception {

        BSFManager bsfManager = new BSFManager();
        ScriptingRuleExecutionOutcome outcome = new ScriptingRuleExecutionOutcomeImpl();


        bsfManager.declareBean("outcome", outcome, ScriptingRuleExecutionOutcomeImpl.class);
        bsfManager.declareBean("log", logger, Log.class);

        for (Iterator<ScriptingRuleParameter> scriptingRuleParameterIterator = ruleParameters.iterator(); scriptingRuleParameterIterator.hasNext();) {
            ScriptingRuleParameter scriptingRuleParameter = scriptingRuleParameterIterator.next();

            bsfManager.declareBean(scriptingRuleParameter.getName(), scriptingRuleParameter.getValue(), scriptingRuleParameter.getType());

        }

        bsfManager.eval(language, "<unknown>", 0, 0, ruleExpression);

        return outcome;
    }


    /**
     * Parses the script name and puts any url parameters in the context
     *
     * @param url     The script url consisting of a path and optional
     *                parameters
     * @param manager The BSF manager to declare new parameters in
     * @return The name of the script to execute
     * @throws Exception If something goes wrong
     */
    protected String parseScriptName(String url, BSFManager manager) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Parsing " + url);
        }
        String name = null;
        if (url != null) {
            String[] parsed = split(url, "?");
            name = parsed[0];
            if (parsed.length == 2) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found a query string");
                }
                String[] args = split(parsed[1], "&");
                for (int x = 0; x < args.length; x++) {
                    String[] param = split(args[x], "=");
                    Object o = manager.lookupBean(param[0]);
                    if (o != null) {
                        logger.warn("BSF variable " + param[0] + " already exists");
                        param[0] = "_" + param[0];
                    }
                    manager.declareBean(param[0], param[1], String.class);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Registering param " + param[0] + " with value " + param[1]);
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No query string:" + parsed.length);
                }
            }
        }
        return name;
    }


    /**
     * Loads the script from cache if possible. Reloads if the script has been
     * recently modified.
     *
     * @param name The name of the script
     * @return The script object
     */
    protected Script loadScript(String name) {

        synchronized (scripts) {

            Script script = (Script) scripts.get(name);
            if (script == null) {
                script = new Script();

                URL scriptUrl = ScriptingRuleEngine.class.getClassLoader().getResource(name);

                try {
                    script.file = new File(scriptUrl.toURI());
                } catch (URISyntaxException e) {
                    script.file = new File(scriptUrl.getPath());
                }

                try {
                    script.lang = BSFManager.getLangFromFilename(script.file.getName());
                } catch (BSFException ex) {
                    logger.warn(ex, ex);
                }
            }

            boolean reloadScript = false;
            long scriptLastModified = script.file.lastModified();
            if (scriptLastModified > script.timeLastLoaded) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Loading updated or new script: " + script.file.getName());
                }
                reloadScript = true;
            }

            if (reloadScript || script.string == null) {
                synchronized (this) {
                    if (reloadScript || script.string == null) {
                        script.timeLastLoaded = System.currentTimeMillis();
                        try {
                            script.string = IOUtils.getStringFromReader(new FileReader(script.file));
                        } catch (IOException ex) {
                            logger.error("Unable to load script", ex);
                        }
                    }
                }
            }

            return script;

        }

    }


    /**
     * Splits a line with the given delimiter
     *
     * @param line      The line to split
     * @param delimiter The string to split with
     * @return An array of substrings
     */
    protected static String[] split(String line, String delimiter) {
        if (line == null || "".equals(line)) {
            return new String[]{};
        }

        List lst = new ArrayList();
        for (Enumeration e = new StringTokenizer(line, delimiter); e.hasMoreElements();) {
            lst.add(e.nextElement());
        }
        String[] ret = new String[lst.size()];
        return (String[]) lst.toArray(ret);
    }


    /**
     * Represents a saved script
     */
    class Script {

        /**
         * The script file
         */
        public File file;

        /**
         * The language the script is in
         */
        public String lang = null;

        /**
         * The time when the script was last used
         */
        public long timeLastLoaded = 0;

        /**
         * The contents of the script file
         */
        public String string = null;
    }


}
