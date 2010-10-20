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

package org.josso.tooling.gshell.core.spring;

import org.apache.geronimo.gshell.shell.Environment;
import org.springframework.aop.TargetSource;

/**
 * A TargetSource that provides an Environment that has to be
 * previously set in a thread local storage.
 */
public class EnvironmentTargetSource implements TargetSource {

    private static ThreadLocal<Environment> tls = new ThreadLocal<Environment>();

    public static Environment getEnvironment() {
        return tls.get();
    }

    public static void setEnvironment(Environment env) {
        tls.set(env);
    }

    public Class getTargetClass() {
        return Environment.class;
    }

    public boolean isStatic() {
        return false;
    }

    public Object getTarget() throws Exception {
        return tls.get();
    }

    public void releaseTarget(Object o) throws Exception {
    }
}