Database = jdbc:oracle:thin:@${tops.database}
Driver = oracle.jdbc.driver.OracleDriver
Username = ${blackjack.database.user}
Password = ${blackjack.database.password}
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2



ClassIData=select nomenclature, meal_type, ui, rotation_day, weight, alternate_name, count_per_ui, unit_of_pack, vol_cubic_feet, cost from Class1_item where  NSN = :nsns

ClassIMenuList = select NSN, nomenclature, rotation_day from Class1_Item where meal_type = :meal and nomenclature = :nomn order by rotation_day

ClassISupplementList = select supplement_item_nsn,  supplement_item_rate from class1_Supplement_Rate where meal_type = :meal and alternate_name = :nomn

Class1ConsumedList = select nsn from class1_item
