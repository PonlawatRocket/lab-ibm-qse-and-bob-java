@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
cd /d "%~dp0"
call mvnw.cmd clean compile
