package org.josso.gateway.identity.service.store.virtual.scripting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.store.virtual.UIDMappingRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingUIDMappingExpression.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="uid-mapping-expression"
 */
public class ScriptingUIDMappingExpression extends BaseExpressionMappingRule implements UIDMappingRule {
    private static final Log logger = LogFactory.getLog(ScriptingUIDMappingExpression.class);

    public Collection<String> select(Collection<String> sourceUids) {

        if (selectExpression != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl("sourceUids", sourceUids, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(selectExpression, language, selectParams);
                return (Collection<String>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Uid Mapping Expression Failed...ignoring", e);
            }
        }

        return sourceUids;
    }

    public String join(Collection<String> selectedUids) {

        if (joinExpression != null) {

            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl("selectedUids", selectedUids, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(joinExpression, language, joinParams);
                return (String) outcome.getObject();
            } catch (Exception e) {
                logger.error("Uid Mapping Expression Failed...ignoring", e);
            }

        }

        return null;
    }

    public String transform(String jointUid) {

        if (transformExpression != null) {
            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl("jointUid", jointUid, String.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(transformExpression, language, transformParams);
                return (String) outcome.getObject();
            } catch (Exception e) {
                logger.error("Uid Mapping Expression Failed...ignoring", e);
            }

        }

        return null;
    }

    public void validate(String transformedUid) {

        if (validateExpression != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl("transformedUid", transformedUid, String.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(validateExpression, language, validateParams);
                Boolean error = (Boolean) outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped uid entry");
                }

            } catch (Exception e) {
                logger.error("Uid Mapping Expression Failed...ignoring", e);
            }
        }
    }

}