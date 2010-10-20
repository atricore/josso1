/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.selfservices.password;

import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.SSONameValuePair;
import org.josso.selfservices.ProcessState;
import org.josso.selfservices.password.lostpassword.LostPasswordProcessState;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mail.MailException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.Message;
import java.io.StringWriter;

/**
 * @org.apache.xbean.XBean element="email-password-distributor"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: EMailPasswordDistributor.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class EMailPasswordDistributor implements PasswordDistributor {

    private static final Log logger = LogFactory.getLog(EMailPasswordDistributor.class);

    private JavaMailSender mailSender;

    // private SimpleMailMessage templateMessage;

    private String mailFrom;

    private String mailSubject;

    private String template;

    /**
     * The SSOUser property that stores the user email address;
     */
    private String mailToUserProperty;

    public void distributePassword(SSOUser user, String clearPassword, ProcessState state) throws PasswordManagementException {

        assert mailToUserProperty != null : "'mailToUserProperty' must be configured!";

        // Need to figure out:
        // 1. recipient to
        // 2. pwd reset url

        // Find out who should receive this mail.
        String mailTo = null;
        SSONameValuePair[] props = user.getProperties();
        for (SSONameValuePair prop : props) {
            if (prop.getName().equals(mailToUserProperty))
                mailTo = prop.getValue();
        }
        if (mailTo == null)
            throw new PasswordManagementException("User property '"+mailToUserProperty+"' not found in SSOUser !");

        // Create the mail text
        String mailText = this.createMessageBody(user, clearPassword, state);

        // Send mail
        sendMail(mailTo, mailFrom, mailText);
    }


    protected void sendMail(final String mailTo, final String mailFrom, final String text) throws PasswordManagementException{

        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws Exception {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
                mimeMessage.setFrom(new InternetAddress(mailFrom));
                mimeMessage.setSubject(getMailSubject());
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(preparator);
        }
        catch (MailException e) {
            throw new PasswordManagementException("Cannot distribute password to ["+mailTo+"] " + e.getMessage(), e);
        }
    }

    protected String createMessageBody(SSOUser user, String clearPassword, ProcessState state) throws PasswordManagementException {

        try {

            if (logger.isDebugEnabled())
                    logger.debug("Creating email body with Velocity template : " + getTemplate());

            // TODO : We should decouple this ..
            String url = (String) ((LostPasswordProcessState)state).getPasswordConfirmUrl();
            assert url == null : "No password confirmation url found in process state, attribute:'passwordConfirmUrl'";

            //create a new instance of the engine
            VelocityEngine ve = new VelocityEngine();

            // configure the engine :

            // Configure engine logger
            ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, logger);

            // Use classloader based resource loader
            ve.setProperty(VelocityEngine.RESOURCE_LOADER, "class");
            ve.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
            ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

            //initialize the engine
            ve.init();

            // Build the proper velocity context
            VelocityContext ctx = new VelocityContext();

            ctx.put("jossoUser", user);
            ctx.put("jossoClearPassword", clearPassword);
            ctx.put("jossoProcessState", state);
            ctx.put("jossoConfirmUrl", url);

            // Find our template
            Template template = ve.getTemplate(this.template);


            // Process current template and produce the proper output.
            // 4Kb
            StringWriter w = new StringWriter(1024 * 4);
            template.merge(ctx, w);
            w.flush();
            return w.getBuffer().toString();

        } catch (ParseErrorException e) {
            throw new PasswordManagementException("Cannot generate e-mail : " + e.getMessage(), e);

        } catch (ResourceNotFoundException e) {
            throw new PasswordManagementException("Cannot generate e-mail : " + e.getMessage(), e);

        } catch (Exception e) {
            throw new PasswordManagementException("Cannot generate e-mail : " + e.getMessage(), e);

        }


    }

    /**
     * @org.apache.xbean.Property alias="mail-sender"
     * @return
     */
    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @org.apache.xbean.Property alias="mail-from"
     * @return
     */
    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    /**
     * @org.apache.xbean.Property alias="mail-subject"
     * @return
     */
    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailToUserProperty() {
        return mailToUserProperty;
    }

    /**
     * @org.apache.xbean.Property alias="mail-to-userproperty"
     * @param mailToUserProperty
     */
    public void setMailToUserProperty(String mailToUserProperty) {
        this.mailToUserProperty = mailToUserProperty;
    }


    /**
     * @org.apache.xbean.Property alias="template"
     * @return
     */
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String velocityTemplate) {
        this.template = velocityTemplate;
    }
}
