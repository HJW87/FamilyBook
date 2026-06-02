#!/bin/sh

MAVEN_PROJECTBASEDIR=$(cd "$(dirname "$0")/.." && pwd)
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set"
    exit 1
fi

JAVA_EXEC="$JAVA_HOME/bin/java"

exec "$JAVA_EXEC" \
  -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  -jar "$MAVEN_WRAPPER_JAR" \
  -DwrapperProperties="$MAVEN_WRAPPER_PROPERTIES" \
  "$@"
