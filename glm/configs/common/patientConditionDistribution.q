Database=jdbc:oracle:thin:@eiger.alpine.bbn.com:1521:alp
Driver = oracle.jdbc.driver.OracleDriver
Username = blackjack8
Password = blackjack8
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

# PATIENT CONDITION DISTRIBUTION QUERY
#
pcQuery = select PATIENT_CONDITION.PC, PC_TITLE, PATIENT_CLUSTER from PATIENT_CONDITION, PATIENT_CLUSTERS where TO_NUMBER(PATIENT_CONDITION.PC)=TO_NUMBER(PATIENT_CLUSTERS.PC(+)) order by PATIENT_CONDITION.PC
distributionQuery = select PC,WIA,NBI,DISEASE from SW_ASIA_PC_DISTRIBUTION order by PC
