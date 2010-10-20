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

package org.josso.selfservices.password.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.junit.Before;
import org.junit.Test;
import org.josso.selfservices.password.PasswordGenerator;
import org.josso.selfservices.password.generator.PasswordGeneratorImpl;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Dec 1, 2008
 * Time: 4:40:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PasswordGeneratorTest {

    private static Log log = LogFactory.getLog(PasswordGeneratorTest.class);

    protected ApplicationContext applicationContext;

    @Before
    public void initAppContext() {
        applicationContext = new ClassPathXmlApplicationContext("/org/josso/selfservices/password/test/josso-passwordgenerator-spring.xml");
    }

    @Test
    public void testPasswordGenerator() throws Exception {

        PasswordGeneratorImpl pwgen = (PasswordGeneratorImpl) applicationContext.getBean("josso-password-generator");
        assert pwgen != null : "No password generator configured";

        String newPassword = pwgen.generateClearPassword();

        log.info("New password ["+newPassword+"]");

        // TODO : Test symbols, digits, case, blacklist , etc

        pwgen.setPasswordLength(9);
        newPassword = pwgen.generateClearPassword();
        assert newPassword.length() == 9 : "Invalid password length";

    }

}
