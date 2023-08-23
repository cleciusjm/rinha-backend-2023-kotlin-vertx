#!/bin/sh

JVM_OPTS="$JVM_OPTS -server"
JVM_OPTS="$JVM_OPTS -Xshareclasses -Xscmx300M"
JVM_OPTS="$JVM_OPTS -Djava.net.preferIPv4Stack=true"
JVM_OPTS="$JVM_OPTS -Djava.awt.headless=true"
JVM_OPTS="$JVM_OPTS -Duser.language=pt -Duser.region=BR -Duser.country=BR"
JVM_OPTS="$JVM_OPTS -Dfile.encoding=UTF8"

echo "Rodando com [JVM_OPTS=$JVM_OPTS] e [OPTS=$1] "

JAVA_OPTS="$JVM_OPTS" java -jar /opt/app.jar $1
