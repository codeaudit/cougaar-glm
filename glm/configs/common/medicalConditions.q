Database=jdbc:oracle:thin:@${org.cougaar.database:eiger.alpine.bbn.com:1521:alp}
Driver = oracle.jdbc.driver.OracleDriver
Username = blackjack8
Password = blackjack8
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

# PATIENT CONDITIONS
#
#conditionQuery = select NOMENCLATURE from PATIENT_CONDITIONS where PATIENTCONDITION = :pc
conditionQuery = select PC_TITLE from PATIENT_CONDITION where PC = :pc

# All Services (for now)
#ClassVIIIMedical=select NSN, DCR from MEDICAL_DCR where PATIENTCONDITION = :pc AND LEVELOFCARE = :loc order by DCR 
#ClassVIIIMedical = select MATERIEL, QUANTITY_MATERIEL_USED from TTT_TREATMENTS where PC = :pc AND LEVEL_OF_CARE = :loc AND (MATERIEL LIKE '6505%' OR MATERIEL LIKE '6510%' OR MATERIEL LIKE '6515%')
#ClassVIIIMedical = select TTT_TREATMENTS.MATERIEL, TTT_TREATMENTS.QUANTITY_MATERIEL_USED, CSG_MASTER.NSN, CSG_MASTER.QUANTITY_USED FROM TTT_TREATMENTS, CSG_MASTER WHERE TTT_TREATMENTS.MATERIEL = CSG_MASTER.CSG (+) AND TTT_TREATMENTS.PC = :pc AND TTT_TREATMENTS.LEVEL_OF_CARE = :loc AND (CSG_MASTER.NSN LIKE '6505%' OR CSG_MASTER.NSN LIKE '6510%' OR CSG_MASTER.NSN LIKE '6515%' OR TTT_TREATMENTS.MATERIEL LIKE '6505%' OR TTT_TREATMENTS.MATERIEL LIKE '6510%' OR TTT_TREATMENTS.MATERIEL LIKE '6515%')
ClassVIIIMedical = select TTT_TREATMENTS.MATERIEL, TTT_TREATMENTS.QUANTITY_MATERIEL_USED, CSG_MASTER.NSN, CSG_MASTER.QUANTITY_USED, TRAY_MASTER.NSN, TRAY_MASTER.TRAY_QUANTITY, TTT_TREATMENTS.PERC_PATIENTS_TREATED FROM TTT_TREATMENTS, CSG_MASTER, TRAY_MASTER WHERE TTT_TREATMENTS.MATERIEL = CSG_MASTER.CSG (+) AND TTT_TREATMENTS.MATERIEL = TRAY_MASTER.TRAY (+) AND TTT_TREATMENTS.PC = :pc AND TTT_TREATMENTS.LEVEL_OF_CARE = :loc AND (TRAY_MASTER.NSN LIKE '6505%' OR TRAY_MASTER.NSN LIKE '6510%' OR TRAY_MASTER.NSN LIKE '6515%' OR CSG_MASTER.NSN LIKE '6505%' OR CSG_MASTER.NSN LIKE '6510%' OR CSG_MASTER.NSN LIKE '6515%' OR TTT_TREATMENTS.MATERIEL LIKE '6505%' OR TTT_TREATMENTS.MATERIEL LIKE '6510%' OR TTT_TREATMENTS.MATERIEL LIKE '6515%')