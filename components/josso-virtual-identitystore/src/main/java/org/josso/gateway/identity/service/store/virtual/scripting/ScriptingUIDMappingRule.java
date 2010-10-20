package org.josso.gateway.identity.service.store.virtual.scripting;

import org.josso.gateway.identity.service.store.virtual.UIDMappingRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @org.apache.xbean.XBean element="uid-mapping-script"

 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingUIDMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class ScriptingUIDMappingRule extends BaseScriptingMappingRule implements UIDMappingRule {
    private static final Log logger = LogFactory.getLog(ScriptingUIDMappingRule.class);

    public Collection<String> select(Collection<String> sourceUids) {

        if (selectRule != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl( "sourceUids", sourceUids, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(selectRule, selectParams);
                return (Collection<String>)outcome.getObject();
            } catch (Exception e) {
                logger.error("Uid Mapping Rule Failed...ignoring", e);
            }
        }

        return sourceUids;
    }

    public String join(Collection<String> selectedUids) {

        if (joinRule != null) {

            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl( "selectedUids", selectedUids, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(joinRule, joinParams);
                return (String)outcome.getObject();
            } catch (Exception e) {
                logger.error("Uid Mapping Rule Failed...ignoring", e);
            }

        }

        return null;
    }

    public String transform(String jointUid) {

        if (transformRule != null) {
            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl( "jointUid", jointUid, String.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(transformRule, transformParams);
                return (String)outcome.getObject();
            } catch (Exception e) {
                logger.error("Uid Mapping Rule Failed...ignoring", e);
            }

        }

        return null;
    }

    public void validate(String transformedUid) {

        if (validateRule != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl( "transformedUid", transformedUid, String.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(validateRule, validateParams);
                Boolean error = (Boolean)outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped uid entry");
                }

            } catch (Exception e) {
                logger.error("Uid Mapping Rule Failed...ignoring", e);
            }
        }
    }

}