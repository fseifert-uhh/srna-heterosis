### sRNA population diversity
mkdir -p ${EXP4_PATH}/srna_population_diversity
java -jar ${TOOL_POPULATION_DIVERSITY} -analysis flintdent -minimumExpression 0.5 -databaseUser ${DATABASE_USER} -databasePassword ${databasePassword} > ${EXP4_PATH}/srna_population_diversity/flint_dent_srna_diversity.csv
java -jar ${TOOL_POPULATION_DIVERSITY} -analysis inbredhybrid -minimumExpression 0.5 -databaseUser ${DATABASE_USER} -databasePassword ${databasePassword} > ${EXP4_PATH}/srna_population_diversity/inbred_hybrid_srna_diversity.csv

mkdir -p ${EXP4_PATH}/srna_inbred_distance
java -jar ${TOOL_INBRED_DISTANCE} -minimumExpression 0.5 -minimumFoldChange 2 -distance binary -databaseUser ${DATABASE_USER} -databasePassword ${databasePassword} > ${EXP4_PATH}/srna_inbred_distance/srna_inbred_binary_distance_expr0_5_fc2.csv
R -f ${TOOL_PCA_INBRED_DISTANCE}