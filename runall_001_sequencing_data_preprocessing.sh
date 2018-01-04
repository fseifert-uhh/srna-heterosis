### sRNA sequencing data processing

## sequence data retrieval

fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016505 && mv ${SEQ_SRNA_DIR}/SRR1016505.fastq ${SEQ_SRNA_DIR}/b73.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016506 && mv ${SEQ_SRNA_DIR}/SRR1016506.fastq ${SEQ_SRNA_DIR}/f037.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016507 && mv ${SEQ_SRNA_DIR}/SRR1016507.fastq ${SEQ_SRNA_DIR}/f039.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016508 && mv ${SEQ_SRNA_DIR}/SRR1016508.fastq ${SEQ_SRNA_DIR}/f043.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016509 && mv ${SEQ_SRNA_DIR}/SRR1016509.fastq ${SEQ_SRNA_DIR}/f047.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016510 && mv ${SEQ_SRNA_DIR}/SRR1016510.fastq ${SEQ_SRNA_DIR}/l024.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016511 && mv ${SEQ_SRNA_DIR}/SRR1016511.fastq ${SEQ_SRNA_DIR}/l035.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016512 && mv ${SEQ_SRNA_DIR}/SRR1016512.fastq ${SEQ_SRNA_DIR}/l043.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016513 && mv ${SEQ_SRNA_DIR}/SRR1016513.fastq ${SEQ_SRNA_DIR}/p033.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016514 && mv ${SEQ_SRNA_DIR}/SRR1016514.fastq ${SEQ_SRNA_DIR}/p040.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016515 && mv ${SEQ_SRNA_DIR}/SRR1016515.fastq ${SEQ_SRNA_DIR}/p046.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016516 && mv ${SEQ_SRNA_DIR}/SRR1016516.fastq ${SEQ_SRNA_DIR}/p048.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016517 && mv ${SEQ_SRNA_DIR}/SRR1016517.fastq ${SEQ_SRNA_DIR}/p063.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016518 && mv ${SEQ_SRNA_DIR}/SRR1016518.fastq ${SEQ_SRNA_DIR}/p066.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016519 && mv ${SEQ_SRNA_DIR}/SRR1016519.fastq ${SEQ_SRNA_DIR}/s028.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016520 && mv ${SEQ_SRNA_DIR}/SRR1016520.fastq ${SEQ_SRNA_DIR}/s036.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016521 && mv ${SEQ_SRNA_DIR}/SRR1016521.fastq ${SEQ_SRNA_DIR}/s044.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016522 && mv ${SEQ_SRNA_DIR}/SRR1016522.fastq ${SEQ_SRNA_DIR}/s046.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016523 && mv ${SEQ_SRNA_DIR}/SRR1016523.fastq ${SEQ_SRNA_DIR}/s049.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016524 && mv ${SEQ_SRNA_DIR}/SRR1016524.fastq ${SEQ_SRNA_DIR}/s050.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016525 && mv ${SEQ_SRNA_DIR}/SRR1016525.fastq ${SEQ_SRNA_DIR}/s058.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016526 && mv ${SEQ_SRNA_DIR}/SRR1016526.fastq ${SEQ_SRNA_DIR}/s067.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016527 && mv ${SEQ_SRNA_DIR}/SRR1016527.fastq ${SEQ_SRNA_DIR}/p033xf047.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016528 && mv ${SEQ_SRNA_DIR}/SRR1016528.fastq ${SEQ_SRNA_DIR}/s028xf039.fastq
fastq-dump -O ${SEQ_SRNA_DIR}/ SRR1016529 && mv ${SEQ_SRNA_DIR}/SRR1016529.fastq ${SEQ_SRNA_DIR}/s028xl024.fastq

## process Illumina read files, filter adapter sequence/quality
java -jar ${TOOL_ILLUMINA_PREPROCESS} -directory ${SEQ_SRNA_DIR} -fileEnding fastq -adapter3Prime TGGAATTCTCGGGTGCCAAGGAACTCCAGTCAC

mkdir -p ${EXP1_PATH}/reads
mv ${SEQ_SRNA_DIR}*_reads.txt ${EXP1_PATH}/reads/

## clustering of redundant reads
R --no-save --args "results/sequences/reads/" < ${TOOL_CLUSTER_READS}

mkdir -p ${EXP1_PATH}/read_counts
mv ${EXP1_PATH}/reads/*_clustered_reads.txt ${EXP1_PATH}/read_counts/

## merging of read counts
java -jar ${TOOL_MERGE_SEQUENCING_DATASETS} -directory ${EXP1_PATH}/read_counts/ -prefix srna_ -suffix _clustered_reads -fileEnding txt

## quantiles normalization
mkdir -p ${EXP1_PATH}/quantile_normalization_read_counts

java -jar ${TOOL_QUANTILE_NORMALIZATION} -fileName ${EXP1_PATH}/read_counts/merged_sequence_count_data.csv -directory ${EXP1_PATH}/quantile_normalization_read_counts/
java -jar ${TOOL_MERGE_SEQUENCING_DATASETS} -directory ${EXP1_PATH}/quantile_normalization_read_counts/ -prefix srna_ -suffix _qnorm -fileEnding _qnorm.csv

## per million reads scaling of quantile normalized read counts
mkdir -p ${EXP1_PATH}/per_million_quantile_normalization_read_counts

java -jar ${TOOL_PER_MILLION_SCALING} -outputDirectory ${EXP1_PATH}/per_million_quantile_normalization_read_counts/ -fileName ${EXP1_PATH}/quantile_normalization_read_counts/merged_sequence_count_data.csv
