#!/bin/sh

source $ALP_INSTALL_PATH/bin/setlibpath.sh
source $ALP_INSTALL_PATH/bin/setarguments.sh

MYCLASSES="org.cougaar.domain.mlm.ui.planviewer.XMLClient"

exec java $MYPROPERTIES -classpath $LIBPATHS $BOOTSTRAPPER $MYCLASSES $*
