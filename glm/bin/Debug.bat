@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %ALP_INSTALL_PATH%\bin\setlibpath.bat

REM runs the XML debugger
set MYCLASSES=org.cougaar.domain.mlm.ui.planviewer.XMLClient

@ECHO ON

java.exe -classpath %LIBPATHS% %MYCLASSES% 

