@echo OFF
CALL %ALP_INSTALL_PATH%\bin\setlibpath.bat
cd %ALP_INSTALL_PATH%\alpine\data\orgview
java -cp %LIBPATHS% org.cougaar.domain.mlm.ui.orgviewServer.NetMapServer
