@echo off

del %ALP_INSTALL_PATH%\fgi\data\850_*.txt
del %ALP_INSTALL_PATH%\fgi\data\AB1_*.txt
del %ALP_INSTALL_PATH%\fgi\data\VENDOR_AE8BV_*.txt
del %ALP_INSTALL_PATH%\fgi\data\VENDOR_AE8BZ_*.txt
del %ALP_INSTALL_PATH%\fgi\data\YPA_*.txt
del %ALP_INSTALL_PATH%\fgi\data\YPB_*.txt
del %ALP_INSTALL_PATH%\fgi\data\DHA_*.txt
del %ALP_INSTALL_PATH%\fgi\data\850intfile.out
del %ALP_INSTALL_PATH%\fgi\data\AB1intfile.out
del %ALP_INSTALL_PATH%\fgi\data\VENDOR_AE8BVintfile.out
del %ALP_INSTALL_PATH%\fgi\data\VENDOR_AE8BZintfile.out
del %ALP_INSTALL_PATH%\fgi\data\YPAintfile.out
del %ALP_INSTALL_PATH%\fgi\data\YPBintfile.out
del %ALP_INSTALL_PATH%\fgi\data\DHAintfile.out
del %ALP_INSTALL_PATH%\fgi\data\MILSTRIP.out

call %ALP_INSTALL_PATH%\bin\setlibpath
call %ALP_INSTALL_PATH%\fgi\data\remoteSQL

sqlplus alp_fgi_demo/alp_fgi_demo@alp-3 @%ALP_INSTALL_PATH%\fgi\data\delete_rules.sql

sqlplus alp_fgi_demo/alp_fgi_demo@alp-3 @%ALP_INSTALL_PATH%\fgi\data\qual_rule.sql

sqlplus alp_fgi_demo/alp_fgi_demo@alp-3 @%ALP_INSTALL_PATH%\fgi\data\qual_rule_test.sql

sqlplus alp_fgi_demo/alp_fgi_demo@alp-3 @%ALP_INSTALL_PATH%\fgi\data\restore_fund_buffer.sql

del *fgi*.log
del t.tmp

start "DLA-Node" node DLANode
