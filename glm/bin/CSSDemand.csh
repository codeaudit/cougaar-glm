#!/bin/csh -f

#source $ALP_INSTALL_PATH/bin/setlibpath.csh

#set MYCLASSES="com.sra.ui.ClusterDisplay"

setenv ALP_DEV_PATH $ALP_INSTALL_PATH/lib/sra.jar
setenv CLASSPATH ${ALP_INSTALL_PATH}/lib/core.jar:${ALP_INSTALL_PATH}/lib/glm.jar:${ALP_INSTALL_PATH}/lib/planserver.jar:${ALP_INSTALL_PATH}/lib/jcchart400K.jar:${ALP_INSTALL_PATH}/lib/xerces.jar:${ALP_DEV_PATH}
set MYCLASSES="org.cougaar.domain.css.ui.ClusterDisplay"


set osargs=""
set os=`uname`
if ("$os" == "Linux") then
  set osargs="-green"
endif

if ($?ALP_DEV_PATH) then
    echo java $osargs -classpath $CLASSPATH $MYCLASSES
endif

set host=$argv[1-]
if ("$host" == "") then
   java  $osargs -classpath $CLASSPATH $MYCLASSES
else
   java  $osargs -classpath $CLASSPATH $MYCLASSES $host 5555
endif
