Database=jdbc:oracle:thin:@eiger.alpine.bbn.com:1521:alp
Driver = oracle.jdbc.driver.OracleDriver
Username = icis
Password = data4icis
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

headerQuery=select commodity, nsn, nomenclature, ui, ssc, price, icc, alt, plt, pcm, boq, diq, iaq, nso, qfd, rop, owrmrp, weight, cube, aac, slq from header where NSN = :nsns
assetsQuery=select nsn, ric, purpose, condition, iaq from assets where NSN = :nsns
nomen=select nomenclature from header where NSN = :nsns	
cost=select price from header where NSN = :nsns
volume=select cube from header where NSN = :nsns
weight=select weight from header where NSN = :nsns
classIXData=select nomenclature, ui, price, cube, weight from header where NSN = :nsns 
classVData=select nomenclature, weight, ccc from ammo_characteristics where DODIC = :nsns
ui=select ui from header where NSN = :nsns
