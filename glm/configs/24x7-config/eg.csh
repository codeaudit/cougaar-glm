#!/bin/tcsh

## Because we are using a different build of eventgen. it is in alpz so set
## alpinstallpath and classpath and path
setsocietyx

setenv JAVA_COMPILER NONE
echo "JIT=" $JAVA_COMPILER

set MYMEMORY="-Xmx768m -Xms64m -Xmaxe64m -Xmine32m -Xoss128k"
set osargs=""
set os=`uname`
if ("$os" == "Linux") then
    # only run with green threads, since SMP is still rare
    set osargs="-green"
    # set some system runtime limits
    limit stacksize 16m    #up from 8m
    limit coredumpsize 0   #down from 1g
    #turn this on to enable inprise JIT
    #setenv JAVA_COMPILER javacomp
endif

##set javaargs="$osargs $MYPROPERTIES $MYMEMORY -classpath $LIBPATHS"
set javaargs="$osargs $MYMEMORY"
## Classpath already set using setsocietyx

# exec instead of eval
## We don't need LIBPATH cause we can use CLASSPATH that is already set.
set cmd="exec java $javaargs org.cougaar.domain.glm.execution.eg.EventGenerator -plugins egplugins.txt"
echo $cmd
$cmd
