@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %ALP_INSTALL_PATH%\bin\setlibpath.bat

REM produces the inventory chart display
set MYCLASSES=org.cougaar.domain.mlm.ui.planviewer.inventory.InventoryChartUI

@ECHO ON

java.exe -classpath %LIBPATHS% %MYCLASSES% %1

