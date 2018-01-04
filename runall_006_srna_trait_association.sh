### sRNA trait association
mkdir -p ${EXP6_PATH}/association

java -jar ${TOOL_SRNA_ASSOCIATION} -minExpression 0.5 -foldChange 2.0 -characteristic MPH -databaseUser ${DATABASE_USER} -databasePassword ${databasePassword} > ${EXP6_PATH}/association/mph_gy_expr0.5_fc2.csv

awk '$2=/positive/{print $1}' ${EXP6_PATH}/association/mph_gy_expr0.5_fc2.csv > ${EXP6_PATH}/association/mph_gy_pos_expr0.5_fc2.csv
awk '$2=/negative/{print $1}' ${EXP6_PATH}/association/mph_gy_expr0.5_fc2.csv > ${EXP6_PATH}/association/mph_gy_neg_expr0.5_fc2.csv
cat ${EXP6_PATH}/association/mph_gy_pos_expr0.5_fc2.csv ${EXP6_PATH}/association/mph_gy_neg_expr0.5_fc2.csv > ${EXP6_PATH}/association/mph_gy_expr0.5_fc2.csv

### sRNA permutation analysis
mkdir -p ${EXP6_PATH}/permutation

java -jar ${TOOL_SRNA_PERMUTATION} -alphaError 0.05 -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} -foldChange 2 -minExpression 0.5 -traitCharacteristic MPH > ${EXP6_PATH}/permutation/mph_gy_expr0.05_fc2_permutation.csv