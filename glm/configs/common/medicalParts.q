#Database=jdbc:oracle:thin:@eiger.alpine.bbn.com:1521:alp
Database=jdbc:oracle:thin:@alp-3.alp.isotic.org:1521:alp
#Database=jdbc:oracle:thin:@${org.cougaar.database:eiger.alpine.bbn.com:1521:alp}
Driver = oracle.jdbc.driver.OracleDriver
Username = blackjack8
Password = blackjack8
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

# classVIIIData=select nomenclature, uoi, price, volume, weight from MEDICAL_SUPPLIES where NSN = :nsns 
classVIIIData=select nomenclature, unit_issue, unit_price, pack_cube, pack_weight from CATALOG_MASTER where NSN = :nsns 
