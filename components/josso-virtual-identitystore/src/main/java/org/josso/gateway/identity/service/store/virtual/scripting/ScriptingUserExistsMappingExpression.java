package org.josso.gateway.identity.service.store.virtual.scripting;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.store.virtual.UserExistsMappingRule;
import org.josso.gateway.identity.service.store.virtual.UserExistsOutcome;

/**
 * @org.apache.xbean.XBean element="user-exists-mapping-expression"
 */
public class ScriptingUserExistsMappingExpression extends BaseExpressionMappingRule implements UserExistsMappingRule {
    private static final Log logger = LogFactory.getLog(ScriptingUserExistsMappingExpression.class);

    public Collection<UserExistsOutcome> select(Collection<UserExistsOutcome> sourceOutcomes) {
        if (selectExpression != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl("sourceOutcomes", sourceOutcomes, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(selectExpression, language, selectParams);
                return (Collection<UserExistsOutcome>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Expression Failed...ignoring", e);
            }
        }

        return sourceOutcomes;
    }

    public UserExistsOutcome join(Collection<UserExistsOutcome> selectedOutcomes) {

        if (joinExpression != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl("selectedOutcomes", selectedOutcomes, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(joinExpression, language, joinParams);
                return (UserExistsOutcome) outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Expression Failed...ignoring", e);
            }
        }

        return null;
    }

    public UserExistsOutcome transform(UserExistsOutcome jointOutcome) {
        if (transformExpression != null) {
            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl("jointOutcome", jointOutcome, UserExistsOutcome.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(transformExpression, language, transformParams);
                return (UserExistsOutcome) outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Expression Failed...ignoring", e);
            }
        }

        return null;
    }

    public void validate(UserExistsOutcome transformedOutcome) {
        if (validateExpression != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl("transformedOutcome", transformedOutcome, UserExistsOutcome.class));

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