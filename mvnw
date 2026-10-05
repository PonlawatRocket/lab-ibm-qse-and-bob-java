#!/bin/sh
# Maven Wrapper script for Unix/macOS
# Generated for NovaPay Mobile Banking QSE Demo
# No Maven installation required — Java 17+ only.

set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(dirname "$0")}"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
DOWNLOAD_URL=$(grep '^distributionUrl=' "$MAVEN_WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')

# Find JAVA_HOME / java
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  echo "[mvnw] Downloading maven-wrapper.jar…"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$(grep '^wrapperUrl=' "$MAVEN_WRAPPER_PROPERTIES" | sed 's/wrapperUrl=//')" -o "$MAVEN_WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$(grep '^wrapperUrl=' "$MAVEN_WRAPPER_PROPERTIES" | sed 's/wrapperUrl=//')" -O "$MAVEN_WRAPPER_JAR"
  fi
fi

exec "$JAVA_CMD" \
  -classpath "$MAVEN_WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
