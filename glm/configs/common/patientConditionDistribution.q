Database=jdbc:oracle:thin:@${org.cougaar.database}
Driver = oracle.jdbc.driver.OracleDriver
Username = ${blackjack8.database.user}
Password = ${blackjack8.database.password}
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

# PATIENT CONDITION DISTRIBUTION QUERY
#
pcQuery = select PATIENT_CONDITION.PC, PC_TITLE, PATIENT_CLUSTER from PATIENT_CONDITION, PATIENT_CLUSTERS where TO_NUMBER(PATIENT_CONDITION.PC)=TO_NUMBER(PATIENT_CLUSTERS.PC(+)) order by PATIENT_CONDITION.PC
#distributionQuery = select PC,WIA,NBI,DISEASE from SW_ASIA_PC_DISTRIBUTION order by PC
#distributionQuery = select PC,WIA,NBI,DISEASE from PATIENT_DISTRIBUTIONS where NAME='PATGEN Default for SW_ASIA region' order by PC
distributionQuery = select PC,WIA,NBI,DISEASE from PATIENT_DISTRIBUTIONS where NAME='PC148 Only' order by PC
