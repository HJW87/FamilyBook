@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0.."
set "MAVEN_WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties"
set "MAVEN_WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar"

if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set
    exit /b 1
)

set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"

%JAVA_EXEC% ^
  -Dmaven.multiModuleProjectDirectory="%CD%" ^
  -jar "%MAVEN_WRAPPER_JAR%" ^
  -DwrapperProperties="%MAVEN_WRAPPER_PROPERTIES%" ^
  %*

endlocal
