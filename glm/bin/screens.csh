#/bin/csh
#
# First column
xterm -sl 50000 -title ADMIN -geometry  80x6+0+0 &
xterm -sl 50000 -title 1BDE -geometry 80x6+0+120 -exec ssh alp-19 &
xterm -sl 50000 -title 2BDE -geometry 80x6+0+230 -exec ssh alp-105 &
xterm -sl 50000 -title 3BDE -geometry 80x6+0+340 -exec ssh alp-103 &
xterm -sl 50000 -title AVN -geometry 80x6+0+450 -exec ssh1 alp-121 &
xterm -sl 50000 -title 3ID -geometry 80x6+0+560 -exec ssh alp-11 &
#
# Second column
xterm -sl 50000 -title SUPPLY -geometry 80x6+520+120 -exec ssh alp-37 &
xterm -sl 50000 -title SUPPORT -geometry 80x6+520+230 -exec ssh alp-17 &
xterm -sl 50000 -title TOPSAirGround -geometry 80x6+520+340 -exec ssh alp-41 &
xterm -sl 50000 -title TOPSPortsTranscap -geometry 80x6+520+450 -exec ssh alp-13 &
xterm -sl 50000 -title TOPSSeaTheater -geometry 80x6+520+560 -exec ssh alp-15 &




