#!/bin/bash

###################################################################################
#
# Creating and deploying JOSSO MBeans for Weblogic 8.1 can be very tricky!
#
# Be careful when modifying this script other than for setting env varibles.
#
# Run inside josso 1.8.4 distribution root folder !!!
#
###################################################################################

# TODO : Create an installer ?

# JOSSO Version and Home folder, modify accordingly but use absolute folder names!

export JOSSO_VERSION="1.8.4"
export JOSSO_HOME=`pwd`

# Weblogic 8.1 Server folder, modify accordingly!
export WL81_HOME="/u01/opt/bea81/weblogic81"

WL81_MBEANS_SRC_FOLDER_NAME="josso-weblogic81-agent-mbeans-src"

export WL81_MBEANS_SRC="$JOSSO_HOME/dist/agents/src/$WL81_MBEANS_SRC_FOLDER_NAME"
# Weblogic 8.1 JDK
export JAVA_HOME=/u01/opt/bea81/jdk142_11

#---------------------------------------------------------------------------------
# Set WL 8.1 Environment
. $WL81_HOME/server/bin/setWLSEnv.sh

# Create our classpath

CLASSPATH="$CLASSPATH:\
$WL81_MBEANS_SRC:\
$WL81_HOME/server/lib/mbeantypes/wlManagement.jar:\
$WL81_HOME/server/lib/mbeantypes/wlMedRecSampleAuthProvider.jar:\
$WL81_HOME/server/lib/mbeantypes/wlSecurityProviders.jar:\
$JOSSO_HOME/dist/agents/lib/josso-weblogic81-agent-$JOSSO_VERSION.jar:\
$JOSSO_HOME/dist/agents/lib/josso-agentj14-shared-$JOSSO_VERSION.jar:\
$JOSSO_HOME/dist/agents/lib/josso-agents-j14bin-$JOSSO_VERSION.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/aopalliance-1.0.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/axis-1.4.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/axis-ant-1.4.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/axis-jaxrpc-1.4.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/axis-saaj-1.4.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/axis-wsdl4j-1.5.1.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-beanutils-1.6.1.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-codec-1.3.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-collections-3.0.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-digester-1.5.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-discovery-0.2.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-lang-2.0.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-logging-1.0.4.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-logging-api-1.0.4.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/commons-modeler-1.1.jar:\
$JOSSO_HOME/dist/agents/bin/3rdparty/spring-aop-2.0.6.jar"

echo '-------------------------------------------------------'
echo JOSSO_HOME=$JOSSO_HOME
echo WL81_HOME=$WL81_HOME
echo WL81_MBEANS_SRC=$WL81_MBEANS_SRC
echo
echo CLASSPATH=$CLASSPATH
echo '-------------------------------------------------------'

currPath=`pwd`

cd $WL81_MBEANS_SRC

echo 'Running WL MBean maker in ' `pwd` 

echo '-------------------------------------------------------'
echo 'Creating MDF File in '`pwd`
$WL81_HOME/../jdk142_11/bin/java -Dfiles=. \
     -DMDF=./JOSSOAuthenticatorProviderImpl.xml \
     -DtargetNameSpace=urn:org:josso:wls81:agent:mbeans \
     -DpreserveStubs=false \
     -DcreateStubs=true \
      weblogic.management.commo.WebLogicMBeanMaker

echo 'Creating MDF File .... DONE!'
echo '-------------------------------------------------------'




echo '-------------------------------------------------------'
echo 'Creating MJF MBean File ....'
$WL81_HOME/../jdk142_11/bin/java -Dfiles=. \
     -DMJF=../josso-weblogic81-agent-mbeans.jar \
     -DpreserveStubs=false \
     -DcreateStubs=true \
      weblogic.management.commo.WebLogicMBeanMaker
echo 'Creating MJF MBean File .... DONE!'
echo '-------------------------------------------------------'

cp ../*-mbeans.jar $currPath/

cd $currPath
