#/bin/csh
#
# First column
xterm -sb -sl 50000 -title 24x7-COMMAND-NODE -geometry 80x24+0+5  -exec ssh alp-115 &
xterm -sb -sl 50000 -title 24x7-1-BDE-NODE -geometry 80x24+0+350  -exec ssh alp-23 &

xterm -sb -sl 50000 -title 24x7-2-BDE-NODE -geometry 80x24+550+5 -exec ssh alp-31 &
xterm -sb -sl 50000 -title 24x7-3-BDE-NODE -geometry 80x24+550+350 -exec ssh alp-43 &

xterm -sb -sl 50000 -title 24x7-1-MEASURE-NODE -geometry 57x9+0+700  -exec ssh alp-23 &
xterm -sb -sl 50000 -title 24x7-2-MEASURE-NODE -geometry 57x9+377+700 -exec ssh alp-31 &
xterm -sb -sl 50000 -title 24x7-3-MEASURE-NODE -geometry 57x9+750+700 -exec ssh alp-43 &

xterm -sb -sl 50000 -title "java Node.tini Tini1 192.168.1.23 1235" -geometry 57x9+0+850  -exec telnet alp-122&
xterm -sb -sl 50000 -title "java Node.tini Tini1 192.168.1.31 1235" -geometry 57x9+377+850 -exec telnet alp-124&
xterm -sb -sl 50000 -title "java Node.tini Tini1 192.168.1.43 1235" -geometry 57x9+750+850 -exec telnet alp-126 &


#
# Second column
xhost + alp-43
set nodeName=`uname -n`
ssh alp-43 xload -display ${nodeName}:0 &
xhost + alp-31
ssh alp-31 xload -display ${nodeName}:0 &
xhost + alp-23
ssh alp-23 xload -display ${nodeName}:0 &
xhost + alp-115
ssh alp-115 xload -display ${nodeName}:0 &
