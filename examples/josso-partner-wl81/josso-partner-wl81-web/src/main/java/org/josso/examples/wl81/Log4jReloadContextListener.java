package org.josso.examples.wl81;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.LogManager;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class Log4jReloadContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {

        /*
        try {
            URL log4jCfg = new URL("file:///u01/opt/bea81/user_projects/domains/jossodomain/log4j.properties");
            InputStream is = log4jCfg.openStream();

            if (is == null)
                servletContextEvent.getServletContext().log("Cannot reset LOG4J Configuration : InputStream not found for " + log4jCfg);
            
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(log4jCfg);

            is.close();
            
        } catch (MalformedURLException e) {
            servletContextEvent.getServletContext().log("Cannot reset LOG4J Configuration : " + e.getMessage());
        } catch (IOException e) {
            servletContextEvent.getServletContext().log("Cannot reset LOG4J Configuration : " + e.getMessage());
        }*/
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        
    }
}
