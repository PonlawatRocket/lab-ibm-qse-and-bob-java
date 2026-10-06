@echo off
echo.
echo  ___________________________________________________
echo
echo      CU Pay Lite Mobile Banking QSE Demo Runner
echo  ___________________________________________________
echo.
echo  No Maven installation required.
echo  Java 17+ must be available on PATH or JAVA_HOME must be set.
echo.
echo  Starting application on http://localhost:8080
echo  Press Ctrl+C to stop.
echo.

@REM Jump to the folder containing this script, then invoke the wrapper
cd /d "%~dp0"
call "%~dp0mvnw.cmd" spring-boot:run 2>&1 | powershell -NoProfile -Command "$input | ForEach-Object { Write-Host $_; if ($_ -match 'Started \w+ in') { Write-Host ''; Write-Host ' [SUCCESS] Application started successfully on http://localhost:8080'; Write-Host '' } }"
