#!/bin/csh -f

source $ALP_INSTALL_PATH/bin/setlibpath.csh

set MYCLASSES="com.sra.ui.ClusterDisplay"

set osargs=""
set os=`uname`
if ("$os" == "Linux") then
  set osargs="-green"
endif

if ($?ALP_DEV_PATH) then
    echo java $osargs -classpath $LIBPATHS $MYCLASSES
endif

set host=$argv[1-]
if ("$host" == "") then
   java  $osargs -classpath $LIBPATHS $MYCLASSES
else
   java  $osargs -classpath $LIBPATHS $MYCLASSES $host 5555
endif
