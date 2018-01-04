### sRNA cluster generation
mkdir -p ${EXP8_PATH}/cluster

mysql -u ${DATABASE_USER} -p ${DATABASE_PASSWORD} < ${DATABASE_DIR}/srna_heterosis_cluster_structure.sql

java -jar ${TOOL_SRNA_CLUSTER} -filename ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.sorted.bam -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} > ${EXP8_PATH}/cluster/srna_cluster_window200_minReadCount5.csv
java -jar ${TOOL_DB_SRNA_CLUSTER} -clusterDataFile ${EXP8_PATH}/cluster/srna_cluster_window200_minReadCount5.csv  -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD}
java -Xms${MAX_RAM}  -jar ${TOOL_DB_CLUSTER_EXPRESSION}

### sRNA cluster/trait association
mkdir -p ${EXP8_PATH}/association

java -jar ${TOOL_SRNA_CLUSTER_ASSOCIATION} -minimumExpression 5 -minumFoldChange 2 -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} > ${EXP8_PATH}/association/mph_gy_cluster_expr5_fc2_bh_correction.csv

awk '$2=/positive/{print$1}' ${EXP8_PATH}/association/mph_gy_cluster_expr5_fc2_bh_correction.csv > ${EXP8_PATH}/association/mph_gy_cluster_expr5_fc2_positive_indices.csv
awk '$2=/negative/{print$1}' ${EXP8_PATH}/association/mph_gy_cluster_expr5_fc2_bh_correction.csv > ${EXP8_PATH}/association/mph_gy_cluster_expr5_fc2_negative_indices.csv
