#!/bin/sh

# This should be sourced by a shell or shell script to define the
# standard contents of an ALP command line.

# Domains are now usually defined by the config file LDMDomains.ini
# But you may still use properties if you wish.
# set MYDOMAINS=-Dalp.domain.alp=org.cougaar.domain.glm.ALPDomain

MYDOMAINS=""
BOOTSTRAPPER=org.cougaar.core.society.Bootstrapper
MYCLASSES=org.cougaar.core.society.Node
if [ "$OS"=="Linux" ]; then
  MYPROPERTIES="-green"
fi
MYPROPERTIES="$MYPROPERTIES $MYDOMAINS  -Dalp.system.path=$ALP3RDPARTY -Dalp.install.path=$ALP_INSTALL_PATH"
MYPROPERTIES="$MYPROPERTIES -Duser.timezone=GMT -Dalp.useBootstrapper=true"
OS=`uname`

MYMEMORY="-Xms100m -Xmx300m"
