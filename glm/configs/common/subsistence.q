Database=jdbc:oracle:thin:@${org.cougaar.database}
Driver = oracle.jdbc.driver.OracleDriver
Username = ${blackjack.database.user}
Username = ${blackjack.database.password}
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

ClassIData=select c.nomenclature, meal_type, ui, rotation_day, weight, r.alternate_name from Class_I_Temp1 c, Class_I_RelationDef r where c.nomenclature = r.nomenclature and c.NSN = :nsns

ClassIMenuList = select NSN from Class_I_Temp1 c where c.meal_type = :meal and c.nomenclature = :nomn
