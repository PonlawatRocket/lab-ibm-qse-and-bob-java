@echo off
echo.
echo  _______________________________________________
echo
echo      NovaPay Mobile Banking QSE Demo Runner
echo  _______________________________________________
echo.
echo  No Maven installation required.
echo  Java 17+ must be available on PATH or JAVA_HOME must be set.
echo.
echo  Starting application on http://localhost:8080
echo  Operator panel:  http://localhost:8080/demo
echo.

@REM Jump to the folder containing this script, then invoke the wrapper
cd /d "%~dp0"
call "%~dp0mvnw.cmd" spring-boot:run
