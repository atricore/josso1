package org.josso.gateway.identity.service.store.virtual.scripting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.store.virtual.BindMappingRule;
import org.josso.gateway.identity.service.store.virtual.BindOutcome;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingBindMappingExpression.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="bind-mapping-expression"
 */
public class ScriptingBindMappingExpression extends BaseExpressionMappingRule implements BindMappingRule {
    private static final Log logger = LogFactory.getLog(ScriptingBindMappingExpression.class);

    public Collection<BindOutcome> select(Collection<BindOutcome> sourceOutcomes) {
        if (selectExpression != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl("sourceOutcomes", sourceOutcomes, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(selectExpression, language, selectParams);
                return (Collection<BindOutcome>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Expression Failed...ignoring", e);
            }
        }

        return sourceOutcomes;
    }

    public BindOutcome join(Collection<BindOutcome> selectedOutcomes) {

        if (joinExpression != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl("selectedOutcomes", selectedOutcomes, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(joinExpression, language, joinParams);
                return (BindOutcome) outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Expression Failed...ignoring", e);
            }
        }

        return null;
    }

    public BindOutcome transform(BindOutcome jointOutcome) {
        if (transformExpression != null) {
            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl("jointOutcome", jointOutcome, BindOutcome.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(transformExpression, language, transformParams);
                return (BindOutcome) outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Expression Failed...ignoring", e);
            }
        }

        return null;
    }

    public void validate(BindOutcome transformedOutcome) {
        if (validateExpression != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl("transformedOutcome", transformedOutcome, BindOutcome.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(validateExpression, language, validateParams);
                Boolean error = (Boolean) outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped outcome entry");
                }

            } catch (Exception e) {
                logger.error("Outcome Mapping Expression Failed...ignoring", e);
            }
        }
    }


}