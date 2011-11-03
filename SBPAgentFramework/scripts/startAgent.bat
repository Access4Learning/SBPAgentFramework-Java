@echo off
set LIB_PATH=..\lib
set CONFIG_PATH=..\config
set EXE_JAR_PATH=..\build\dist

set JVM_SETTINGS=

rem ####################################
rem # JVM settings for proxy tunneling
rem ####################################
rem set JVM_SETTINGS=%JVM_SETTINGS% -Dhttp.proxyHost=<IP ADDRESS> -Dhttp.proxyPort=<PORT> -Dhttps.proxyHost=<IP ADDRESS> -Dhttps.proxyPort=<PORT>

rem ####################################
rem # JVM settings for ignore proxy IPs
rem ####################################
rem set NO_PROXY=-Dhttp.nonProxyHosts=<MACHINE NAME>|<MACHINE NAME>|...

set NO_PROXY=
set JVM_SETTINGS=%JVM_SETTINGS% %NO_PROXY%

rem ##############################
rem # JVM Memory settings
rem ##############################

set JVM_SETTINGS=%JVM_SETTINGS% -Xms128m -Xmx512m -Xss256k -XX:MaxPermSize=64m

rem #######################################
rem # Class Path including all libraries
rem #######################################

set AGENT_CLASS_PATH=

SETLOCAL ENABLEDELAYEDEXPANSION
for /f %%a IN ('dir /b /S %LIB_PATH%\*.jar') do set AGENT_CLASS_PATH=!AGENT_CLASS_PATH!;%%a

rem ######################################################################
rem # set the config dir and the main executable jar in the classpath
rem #######################################################################

set AGENT_CLASS_PATH=%AGENT_CLASS_PATH%;%CONFIG_PATH%;%EXE_JAR_PATH%\sbpframework-au_1.1-v1.0-beta.jar

set AGENT_CLASS=%1%
set AGENT_ID=%2%

echo ======================================================================================================
echo Start Agent with JVM Settings:
echo %JVM_SETTINGS%
echo ======================================================================================================
echo Start Agent with Classpath:
echo %AGENT_CLASS_PATH%
echo Name of SBP Agent Class: %AGENT_CLASS%
echo Name of Agent: %AGENT_ID%
echo ======================================================================================================

%JAVA_HOME%\bin\java %JVM_SETTINGS% -cp %AGENT_CLASS_PATH% systemic.sif.sbpframework.agent.%AGENT_CLASS% %AGENT_ID%