#!/bin/sh

##############################
# Some environment variables
##############################

CURRENT_DIR=`pwd`

BASE_PATH=../
LIB_PATH=$BASE_PATH/lib
CONFIG_PATH=$BASE_PATH/config
PROJ_JAR_PATH=$BASE_PATH/build/dist

####################################
# JVM settings for proxy tunneling
####################################
JVM_SETTINGS=
#JVM_SETTINGS="${JVM_SETTINGS} -Dhttp.proxyHost=<IP ADDRESS> -Dhttp.proxyPort=<PORT> -Dhttps.proxyHost=<IP ADDRESS> -Dhttps.proxyPort=<PORT>"

####################################
# JVM settings for ignore proxy IPs
####################################
NO_PROXY=
#NO_PROXY=-Dhttp.nonProxyHosts="\"<MACHINE NAME>|<MACHINE NAME>|...\""
JVM_SETTINGS="${JVM_SETTINGS} ${NO_PROXY}"

##############################
# JVM Memory settings
##############################
JVM_SETTINGS="${JVM_SETTINGS} -Xms128m -Xmx512m -Xss256k -XX:MaxPermSize=64m"

#######################################
# Class Path including all libraries
#######################################
AGENT_CLASS_PATH=

cd $LIB_PATH
THIS_DIR=`pwd`
jarfiles=`find $THIS_DIR -name "*.jar"`
cd $CURRENT_DIR

for jarfile in $jarfiles;
do
  AGENT_CLASS_PATH="${AGENT_CLASS_PATH}:${jarfile}"
done

#########################################
# Add config directories to classpath
#########################################
AGENT_CLASS_PATH=$AGENT_CLASS_PATH:$CONFIG_PATH:$PROJ_JAR_PATH/sbpframework-au_1.1-v1.0-beta.jar

echo ========================================================
echo Classpath: $AGENT_CLASS_PATH
echo ========================================================
echo JVM Settings: $JVM_SETTINGS
echo JAVA_HOME: $JAVA_HOME
echo Name of SBP Agent Class: $1
echo Agent to Start: $2
echo ========================================================

$JAVA_HOME/bin/java ${JVM_SETTINGS} -cp ${AGENT_CLASS_PATH} systemic.sif.sbpframework.agent.$1 $2