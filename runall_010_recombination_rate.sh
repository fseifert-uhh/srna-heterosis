### recombination_rate calculation
mkdir -p ${EXP10_PATH}

R -f ${TOOL_SRNA_LOWESS_GAM}
R -f ${TOOL_RECOMBINATION_RATE}

cat ${EXP10_PATH}/chr1_map_lowess_znorm.csv > ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr2_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr3_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr4_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr5_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr6_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr7_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr8_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr9_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv
cat ${EXP10_PATH}/chr10_map_lowess_znorm.csv >> ${EXP10_PATH}/recombination_rate.csv

