#!/bin/bash

###################################################################################
#
# Creating and deploying JOSSO MBeans for Weblogic 8.1 can be very tricky!
#
# Be careful when modifying this script other than for setting env varibles.
#
# Run inside josso 1.8.5 distribution root folder !!!
#
###################################################################################

export JOSSO_VERSION="1.8.5"
export JOSSO_HOME=`pwd`

mkdir tmp

cd tmp

# Create MBeans here!
jar xvf $JOSSO_HOME/dist/agents/lib/josso-weblogic81-agent-$JOSSO_VERSION.jar
jar xvf $JOSSO_HOME/dist/agents/lib/josso-agent-j14compat-$JOSSO_VERSION.jar
jar xvf $JOSSO_HOME/dist/agents/lib/josso-ws-$JOSSO_VERSION.jar
jar xvf $JOSSO_HOME/dist/agents/lib/josso-common-$JOSSO_VERSION.jar
jar xvf $JOSSO_HOME/dist/agents/bin/3rdparty/commons-lang-2.0.jar
jar xvf $JOSSO_HOME/dist/agents/bin/3rdparty/commons-collections-3.0.jar
jar xvf $JOSSO_HOME/dist/agents/bin/3rdparty/commons-digester-1.5.jar
jar xvf $JOSSO_HOME/dist/agents/bin/3rdparty/commons-discovery-0.2.jar
jar xvf $JOSSO_HOME/dist/agents/bin/3rdparty/spring-2.0.6.jar

jar xvf ../josso-weblogic81-agent-mbeans.jar

rm META-INF/MANIFEST.MF

jar cvf ../josso-weblogic81-agent-mbeans.jar *

cd ..