@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %ALP_INSTALL_PATH%\bin\setlibpath.bat

REM produces the inventory chart display
set MYCLASSES=ui.planviewer.stoplight.SupplyController
set MYPROPERTIES=-Dalp.install.path=%ALP_INSTALL_PATH%

@ECHO ON
java.exe %MYPROPERTIES% -classpath %LIBPATHS% %MYCLASSES% %1

