package org.josso.gateway.identity.service.store.virtual.scripting;

import org.josso.gateway.identity.service.store.virtual.BindMappingRule;
import org.josso.gateway.identity.service.store.virtual.BindOutcome;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @org.apache.xbean.XBean element="bind-mapping-script"
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ScriptingBindMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 */
public class ScriptingBindMappingRule extends BaseScriptingMappingRule implements BindMappingRule {
    private static final Log logger = LogFactory.getLog(ScriptingBindMappingRule.class);

    public Collection<BindOutcome> select(Collection<BindOutcome> sourceOutcomes) {
        if (selectRule != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl( "sourceOutcomes", sourceOutcomes, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(selectRule, selectParams);
                return (Collection<BindOutcome>)outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Rule Failed...ignoring", e);
            }
        }

        return sourceOutcomes;
    }

    public BindOutcome join(Collection<BindOutcome> selectedOutcomes) {

        if (joinRule != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl( "selectedOutcomes", selectedOutcomes, Collection.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(joinRule, joinParams);
                return (BindOutcome)outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Rule Failed...ignoring", e);
            }
        }

        return null;
    }

    public BindOutcome transform(BindOutcome jointOutcome) {
        if (transformRule != null) {
            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl( "jointOutcome", jointOutcome, BindOutcome.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(transformRule, transformParams);
                return (BindOutcome)outcome.getObject();
            } catch (Exception e) {
                logger.error("Outcome Mapping Rule Failed...ignoring", e);
            }
        }

        return null;
    }

    public void validate(BindOutcome transformedOutcome) {
        if (validateRule != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl( "transformedOutcome", transformedOutcome, BindOutcome.class ));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.execute(validateRule, validateParams);
                Boolean error = (Boolean)outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped outcome entry");
                }

            } catch (Exception e) {
                logger.error("Outcome Mapping Rule Failed...ignoring", e);
            }
        }
    }

    public String getSelectRule() {
        return selectRule;
    }

}