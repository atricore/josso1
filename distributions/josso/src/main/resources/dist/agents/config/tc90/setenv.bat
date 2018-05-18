set JAVA_OPTS=%JAVA_OPTS% -Djava.security.auth.login.config=%CATALINA_HOME%\conf\jaas.conf

REM Enable the jconsole agent locally
REM set JAVA_OPTS=%$JAVA_OPTS% -Dcom.sun.management.jmxremote
