@echo off

del %COUGAAR_INSTALL_PATH%\fgi\data\850_*.txt
del %COUGAAR_INSTALL_PATH%\fgi\data\AB1_*.txt
del %COUGAAR_INSTALL_PATH%\fgi\data\VENDOR_AE8BV_*.txt
del %COUGAAR_INSTALL_PATH%\fgi\data\VENDOR_AE8BZ_*.txt
del %COUGAAR_INSTALL_PATH%\fgi\data\YPA_*.txt
del %COUGAAR_INSTALL_PATH%\fgi\data\YPB_*.txt
del %COUGAAR_INSTALL_PATH%\fgi\data\DHA_*.txt
del %COUGAAR_INSTALL_PATH%\fgi\data\850intfile.out
del %COUGAAR_INSTALL_PATH%\fgi\data\AB1intfile.out
del %COUGAAR_INSTALL_PATH%\fgi\data\VENDOR_AE8BVintfile.out
del %COUGAAR_INSTALL_PATH%\fgi\data\VENDOR_AE8BZintfile.out
del %COUGAAR_INSTALL_PATH%\fgi\data\YPAintfile.out
del %COUGAAR_INSTALL_PATH%\fgi\data\YPBintfile.out
del %COUGAAR_INSTALL_PATH%\fgi\data\DHAintfile.out
del %COUGAAR_INSTALL_PATH%\fgi\data\MILSTRIP.out

copy %COUGAAR_INSTALL_PATH%\fgi\data\DistFGIPluginProperties.ini %COUGAAR_INSTALL_PATH%\fgi\data\FGIPluginProperties.ini
copy %COUGAAR_INSTALL_PATH%\fgi\data\DistRemoteSQL.ini %COUGAAR_INSTALL_PATH%\fgi\data\RemoteSQL.ini

call %COUGAAR_INSTALL_PATH%\bin\setlibpath
call %COUGAAR_INSTALL_PATH%\fgi\data\remoteSQL

sqlplus alp_fgi/alp_fgi@alp-3 @%COUGAAR_INSTALL_PATH%\fgi\data\delete_rules.sql

sqlplus alp_fgi/alp_fgi@alp-3 @%COUGAAR_INSTALL_PATH%\fgi\data\qual_rule.sql

sqlplus alp_fgi/alp_fgi@alp-3 @%COUGAAR_INSTALL_PATH%\fgi\data\qual_rule_test.sql

sqlplus alp_fgi/alp_fgi@alp-3 @%COUGAAR_INSTALL_PATH%\fgi\data\restore_fund_buffer.sql

del *fgi*.log
del t.tmp

start "DLA-Node" node DLANode
