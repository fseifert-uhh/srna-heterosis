###  database generation
mysql -u ${DATABASE_USER} -p ${DATABASE_PASSWORD} < ${DATABASE_DIR}/srna_heterosis_sequence.sql
mysql -u ${DATABASE_USER} -p ${DATABASE_PASSWORD} < ${DATABASE_DIR}/srna_heterosis_germplasm.sql

java -jar ${TOOL_DB_SEQUENCE_IMPORT} -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} -expressionDataFile ${EXP1_PATH}/quantile_normalization_read_counts/merged_sequence_count_data.csv
java -jar ${TOOL_DB_EXPRESSION_IMPORT} -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} -databaseTable srna_library_expression -expressionDataFile ${EXP1_PATH}/quantile_normalization_read_counts/merged_sequence_count_data.csv

mkdir -p ${EXP2_PATH}/srna_fasta
java -jar ${TOOL_DB_SEQUENCE_EXPORT} -outputFileName ${EXP2_PATH}/srna_fasta/srna_heterosis_reads.fasta

### genome annotation processing
cat ${GENE_GFF3} | awk '($3 ~ /gene/) {print $0}' > ${EXP2_PATH}/gene.gff3 
cat ${REPEAT_GFF3} | awk '($1 !~ /###/) {print $0}' > ${EXP2_PATH}/repeat.gff3 
cat ${EXP2_PATH}/gene.gff3 ${EXP2_PATH}/repeat.gff3  | sort -k1,1 -k4,4n > ${EXP2_PATH}/annotation.gff3 

mysql -u ${DATABASE_USER} -p ${DATABASE_PASSWORD} < ${DATABASE_DIR}/srna_heterosis_genome_annotation.sql



