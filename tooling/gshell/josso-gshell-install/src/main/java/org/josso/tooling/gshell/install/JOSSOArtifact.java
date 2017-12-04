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

package org.josso.tooling.gshell.install;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 21, 2008
 * Time: 1:26:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class JOSSOArtifact {

    public JOSSOArtifact(String id, String version, String classifier, String type, JOSSOScope jossoScope, String baseUrl) {
        this.id = id;
        this.version = version;
        this.type = type;
        this.jossoScope = jossoScope;
        this.classifier = classifier;
        this.location = baseUrl + (baseUrl.endsWith("/") ? "" : "/")  + id + "-" + version + "." + type;
    }

    public JOSSOArtifact(JOSSOScope jossoScope, String location) {

        // Base name : artifact-version<-classifier>.type

        int baseNameFrom = location.lastIndexOf("/");
        if (baseNameFrom < 0) baseNameFrom = 0;
        String baseName = location.substring(baseNameFrom + 1);


        if (baseName.endsWith(".jar")) {
            this.type = "jar";
            baseName = baseName.substring(0, baseName.length() - 4);
        } else if (baseName.endsWith(".car")) {
            this.type = "car";
            baseName = baseName.substring(0, baseName.length() - 4);
        } else if (baseName.endsWith(".zip")) {
            this.type = "zip";
            baseName = baseName.substring(0, baseName.length() - 7);
        } else if (baseName.endsWith(".tar.gz")) {
            this.type = "tar.gz";
            baseName = baseName.substring(0, baseName.length() - 4);
        } else if (baseName.endsWith(".dll")) {
            this.type = "dll";
            baseName = baseName.substring(0, baseName.length() - 4);
        } else {

            int typeFrom = baseName.lastIndexOf(".");
            if (typeFrom >= 0) {
                this.type = baseName.substring(typeFrom + 1);
                baseName = baseName.substring(0, typeFrom);
            }
        }

        // Get classifier/version

        int versionFrom = baseName.lastIndexOf("-");
        if (versionFrom >= 0) {

            this.version = baseName.substring(versionFrom + 1);
            // check if this is a version, it MUST contain a dot at least.
            baseName = baseName.substring(0, versionFrom);

            boolean snapshot = false;

            if (!version.contains(".")) {

                if (!this.version.equals("SNAPSHOT")) {
                    this.classifier = this.version;
                } else {
                    snapshot = true;
                }

                versionFrom = baseName.lastIndexOf("-");
                if (versionFrom >= 0) {

                    this.version = baseName.substring(versionFrom + 1);
                    baseName = baseName.substring(0, versionFrom);

                    if (!version.contains(".")) {

                        if (this.version.equals("SNAPSHOT")) {
                            snapshot = true;
                        }

                        versionFrom = baseName.lastIndexOf("-");
                        if (versionFrom >= 0) {
                            this.version = baseName.substring(versionFrom + 1);
                            baseName = baseName.substring(0, versionFrom);

                        }

                        if (snapshot) {
                            version += "-SNAPSHOT";
                        }

                    }

                }
            }

        }
        
        this.id = baseName;
        this.location = location;
        this.jossoScope = jossoScope;

    }

    // Name
    private String id;
    
    private String classifier;

    private String version;

    // Type of artifact : dependency, config, application, component 
    private String type;

    // agent , gateway
    private JOSSOScope jossoScope;

    // artifact locatin (url)
    private String location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JOSSOScope getJossoScope() {
        return jossoScope;
    }

    public void setJOSSOScope(JOSSOScope jossoScope) {
        this.jossoScope = jossoScope;
    }
    
    public String getBaseName() {
        return id + "-" + version + "." + type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
         return id + (version != null ? "-" + version : "") + (classifier != null ? "-" + classifier : "") + (type != null ? "." + type : "") + " ["+location+"]";
    }
}
