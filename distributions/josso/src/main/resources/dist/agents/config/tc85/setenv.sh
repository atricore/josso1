JAVA_OPTS="$JAVA_OPTS -Djava.security.auth.login.config=$CATALINA_HOME/conf/jaas.conf"

# Enable the jconsole agent locally
# JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"

export JAVA_OPTS