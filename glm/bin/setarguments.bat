@echo OFF

rem Domains are now usually defined by the config file LDMDomains.ini
rem But you may still use properties if you wish.
rem set MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.domain.glm.ALPDomain
set MYDOMAINS=
set MYCLASSES=org.cougaar.core.society.Node
set MYPROPERTIES=%MYDOMAINS% -Dorg.cougaar.system.path=%ALP3RDPARTY% -Dorg.cougaar.install.path=%ALP_INSTALL_PATH% -Duser.timezone=GMT
set MYMEMORY=-Xms100m -Xmx300m

