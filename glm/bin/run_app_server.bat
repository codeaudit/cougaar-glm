set Path=%ALP_INSTALL_PATH%\bin;%Path%
CALL %ALP_INSTALL_PATH%\bin\setlibpath.bat
java -classpath %LIBPATHS% -Dalp.install.path=%ALP_INSTALL_PATH% org.cougaar.appserver.ApplicationServer 8001