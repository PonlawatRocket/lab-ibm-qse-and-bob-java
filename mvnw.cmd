@REM Maven Wrapper script for Windows
@REM Generated for NovaPay Mobile Banking QSE Demo
@REM No Maven installation required — Java 17+ only.
@REM
@REM Usage (from inside the project directory):
@REM   mvnw.cmd clean compile
@REM   mvnw.cmd spring-boot:run
@REM   mvnw.cmd package

@echo off
setlocal EnableDelayedExpansion

@REM Always run from the directory that contains this script
cd /d "%~dp0"

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

@REM Locate java executable
if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java.exe"
)

@REM Verify java is available
"%JAVA_EXE%" -version >NUL 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Java not found.
    echo Please install Java 17 and ensure it is on your PATH or set JAVA_HOME.
    echo.
    exit /b 1
)

@REM Download wrapper JAR if missing
if not exist "%WRAPPER_JAR%" (
    echo Downloading maven-wrapper.jar...
    for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
        if "%%A"=="wrapperUrl" set "WRAPPER_URL=%%B"
    )
    powershell -Command "Invoke-WebRequest -Uri '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%' -UseBasicParsing"
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
