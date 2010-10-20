package org.josso.gateway.identity.service.store.virtual.scripting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.Credential;
import org.josso.gateway.identity.service.store.virtual.CredentialMappingRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: ExpressionCredentialMappingRule.java 1644 2010-07-27 19:31:39Z sgonzalez $
 * @org.apache.xbean.XBean element="credential-mapping-expression"
 */
public class ExpressionCredentialMappingRule extends BaseExpressionMappingRule implements CredentialMappingRule {
    private static final Log logger = LogFactory.getLog(ExpressionCredentialMappingRule.class);

    public Collection<Credential> select(Collection<Credential> sourceCredentials) {

        if (selectExpression != null) {
            Collection<ScriptingRuleParameter> selectParams = new ArrayList<ScriptingRuleParameter>();

            selectParams.add(new ScriptingRuleParameterImpl("sourceCredentials", sourceCredentials, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(selectExpression, language, selectParams);
                return (Collection<Credential>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Credential Mapping Expression Failed...ignoring", e);
            }
        }

        return sourceCredentials;
    }

    public Collection<Credential> join(Collection<Credential> selectedCredentials) {

        if (joinExpression != null) {
            Collection<ScriptingRuleParameter> joinParams = new ArrayList<ScriptingRuleParameter>();

            joinParams.add(new ScriptingRuleParameterImpl("selectedCredentials", selectedCredentials, Collection.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(joinExpression, language, joinParams);
                return (Collection<Credential>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Credential Mapping Expression Failed...ignoring", e);
            }

        }

        return null;
    }

    public Collection<Credential> transform(Collection<Credential> jointCredential) {

        if (transformExpression != null) {

            Collection<ScriptingRuleParameter> transformParams = new ArrayList<ScriptingRuleParameter>();

            transformParams.add(new ScriptingRuleParameterImpl("jointCredential", jointCredential, Credential.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(transformExpression, language, transformParams);
                return (Collection<Credential>) outcome.getObject();
            } catch (Exception e) {
                logger.error("Credential Mapping Expression Failed...ignoring", e);
            }
        }


        return null;
    }

    public void validate(Collection<Credential> transformedCredentials) {

        if (validateExpression != null) {
            Collection<ScriptingRuleParameter> validateParams = new ArrayList<ScriptingRuleParameter>();

            validateParams.add(new ScriptingRuleParameterImpl("transformedCredentials", transformedCredentials, Credential.class));

            ScriptingRuleEngine sre = new ScriptingRuleEngine();
            ScriptingRuleExecutionOutcome outcome;

            try {
                outcome = sre.evaluate(validateExpression, language, validateParams);
                Boolean error = (Boolean) outcome.getObject();

                if (error) {
                    throw new IllegalArgumentException("Error validating mapped credential entry");
                }

            } catch (Exception e) {
                logger.error("Credential Mapping Expression Failed...ignoring", e);
            }
        }
    }

}