### Comparison of euclidean distance trait correlation for mRNA, SNP and sRNAs
## Import of Microarray and SNP-array data
mysql -u ${DATABASE_USER} -p ${DATABASE_PASSWORD} < ${DATABASE_DIR}/srna_heterosis_microarray_data.sql
mysql -u ${DATABASE_USER} -p ${DATABASE_PASSWORD} < ${DATABASE_DIR}/srna_heterosis_snp_data.sql

## Feature/trait correlation analysis
# SNP data
mkdir -p ${EXP5_PATH}/snp
java -jar ${TOOL_SNP_CORRELATION} -characteristic MPH -fdrLimit 1 -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} > ${EXP5_PATH}/snp/mph_snp.csv

# Microarray (mRNA) data
mkdir -p ${EXP5_PATH}/mRNA
java -jar ${TOOL_SNP_CORRELATION} -F 100 -u ${DATABASE_USER} -p ${DATABASE_PASSWORD} > ${EXP5_PATH}/mrna/mph_mrna.csv

# sRNA data
mkdir -p ${EXP5_PATH}/srna
java -jar ${TOOL_SRNA_CORRELATION} -minimumExpression 0.5 -minimumFoldChange 2 -fdrCorrection n -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} > ${EXP5_PATH}/srna/mph_srna.csv
java -jar ${TOOL_SRNA_CORRELATION} -minimumExpression 0.5 -minimumFoldChange 2 -fdrCorrection n -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} > ${EXP5_PATH}/srna/mph_srna.csv