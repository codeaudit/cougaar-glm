@echo ON 

REM calls setlibpath.bat which sets the path to the required jar files.
REM calls setarguments.bat which sets input parameters for system behavior
CALL %ALP_INSTALL_PATH%\bin\setlibpath.bat
CALL %ALP_INSTALL_PATH%\bin\setarguments.bat

set HOST=%1
echo %HOST%
set PORT=5555
if "%1"=="" set MYARGUMENTS=    
if not "%1"=="" set MYARGUMENTS=%HOST% %PORT%

@ECHO ON

java.exe -classpath %LIBPATHS% com.sra.ui.ClusterDisplay %MYARGUMENTS% 

