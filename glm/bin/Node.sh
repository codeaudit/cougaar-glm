#!/bin/sh

# Environment variables
# ALP_INSTALL_PATH = the head of the alp install.
# ALP3RDPARTY = a directory containing 3rd party jar files
#
# ALP bootstrapping classpath will be:
#  $ALP_DEV_PATH	if defined
#  $ALP_INSTALL_PATH/lib/core.jar
#
# once running, jar files will be searched for in (in order):
#  -Dalp.class.path 	like classpath
#  $CLASSPATH		(alp bootstrapping path from above)
#  $ALP_INSTALL_PATH/lib/*
#  $ALP_INSTALL_PATH/plugins/*
#  -Dalp.system.path=$ALP3RDPARTY
#  $ALP_INSTALL_PATH/sys/*
#

source $ALP_INSTALL_PATH/bin/setlibpath.sh
source $ALP_INSTALL_PATH/bin/setarguments.sh

node="$1"
shift
rest="$*"
if [ -z "$node" ]; then
  node="Clusters"
fi

if [ "$node" = "admin" ]; then
    args="-c -r -n Administrator -p 8000 $rest"
    MYMEMORY="-Djava.compiler=NONE"
else 
    args="-n $node -c $rest"
    # arguments to adjust (defaults are given)
    # -Xmx64m	      # max java heap
    # -Xms3m	      # min (initial) java heap
    # -Xmaxf0.6       # max heap free percent
    # -Xminf0.35      # min heap free percent
    # -Xmaxe4m        # max heap expansion increment
    # -Xmine1m	      # min heap expansion increment
    # -Xoss400k       # per-thread *java* stack size
    MYMEMORY="-Xmx768m -Xms64m -Xmaxf0.9 -Xminf0.1 -Xoss128k"
    #set MYMEMORY="-Xmx300m -Xms100m"
fi

if [ "$OS" = "Linux" ]; then
    # set some system runtime limits
    limit stacksize 16m    #up from 8m
    limit coredumpsize 0   #down from 1g
    #turn this on to enable inprise JIT
    #setenv JAVA_COMPILER javacomp
fi

#set javaargs="$osargs $MYPROPERTIES $MYMEMORY -classpath $LIBPATHS -Dalp.message.isLogging=true -Djava.rmi.server.logCalls=true -Dsun.rmi.server.exceptionTrace=true -Dsun.rmi.transport.tcp.readTimeout=150000 "
javaargs="$MYPROPERTIES $MYMEMORY -classpath $LIBPATHS"

if [ "$ALP_DEV_PATH" != "" ]; then
    echo java $javaargs org.cougaar.core.society.Node $args
fi

# exec instead of eval
exec java $javaargs org.cougaar.core.society.Node $args
