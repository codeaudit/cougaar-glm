@echo OFF
CALL %ALP_INSTALL_PATH%\bin\setlibpath.bat
@echo ON

java -classpath %LIBPATHS%   org.cougaar.domain.mlm.ui.views.PolicyApplication
