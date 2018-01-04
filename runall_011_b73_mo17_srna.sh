### Regulski et al. 2013 - B73/Mo17 sRNA sequencing data
## data retrieval

fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521111
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521112
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521113
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521114
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521115
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521116
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521117
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521118
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521119
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521120
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521121
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521122
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1 SRR521123
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958936_B73_smrna_repl2 SRR521124
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958937_B73_smrna_repl3 SRR521125
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521126
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521127
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521128
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521129
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521130
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521131
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521132
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521133 
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521134
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521135
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521136
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521137 
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1 SRR521138
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958939_Mo17_smrna_repl2 SRR521139
fastq-dump -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958940_Mo17_smrna_repl3 SRR521140.sra 

## sequencing data processing
mkdir -p ${EXP11_PATH}

TOOL_ILLUMINA_TRIM="src/IlluminaReadPreprocess/dist/IlluminaReadPreprocess.jar"

java -jar ${TOOL_ILLUMINA_PREPROCESS} -directory ${SEQ_B73_MO17_SRNA_DIR}/GSM858935_B73_smrna_repl1/ -fileEnding fastq -adapter3Prime TCGTATGCCGT
java -jar ${TOOL_ILLUMINA_PREPROCESS} -directory ${SEQ_B73_MO17_SRNA_DIR}/GSM958936_B73_smrna_repl2/ -fileEnding fastq -adapter3Prime TCGTATGCCGT
java -jar ${TOOL_ILLUMINA_PREPROCESS} -directory ${SEQ_B73_MO17_SRNA_DIR}/GSM958937_B73_smrna_repl3/ -fileEnding fastq -adapter3Prime TCGTATGCCGT
java -jar ${TOOL_ILLUMINA_PREPROCESS} -directory ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1/ -fileEnding fastq -adapter3Prime TCGTATGCCGT
java -jar ${TOOL_ILLUMINA_PREPROCESS} -directory ${SEQ_B73_MO17_SRNA_DIR}/GSM958939_Mo17_smrna_repl2/ -fileEnding fastq -adapter3Prime TCGTATGCCGT
java -jar ${TOOL_ILLUMINA_PREPROCESS} -directory ${SEQ_B73_MO17_SRNA_DIR}/GSM958940_Mo17_smrna_repl3/ -fileEnding fastq -adapter3Prime TCGTATGCCGT

cat ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1/SRR5211*_reads.txt > ${SEQ_B73_MO17_SRNA_DIR}/GSM958935_B73_smrna_repl1_reads.txt
cat ${SEQ_B73_MO17_SRNA_DIR}/GSM958936_B73_smrna_repl2/SRR5211*_reads.txt > ${SEQ_B73_MO17_SRNA_DIR}/GSM958936_B73_smrna_repl2_reads.txt
cat ${SEQ_B73_MO17_SRNA_DIR}/GSM958937_B73_smrna_repl3/SRR5211*_reads.txt > ${SEQ_B73_MO17_SRNA_DIR}/GSM958937_B73_smrna_repl3_reads.txt
cat ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1/SRR5211*_reads.txt > ${SEQ_B73_MO17_SRNA_DIR}/GSM958938_Mo17_smrna_repl1_reads.txt
cat ${SEQ_B73_MO17_SRNA_DIR}/GSM958939_Mo17_smrna_repl2/SRR5211*_reads.txt > ${SEQ_B73_MO17_SRNA_DIR}/GSM958939_Mo17_smrna_repl2_reads.txt
cat ${SEQ_B73_MO17_SRNA_DIR}/GSM958940_Mo17_smrna_repl3/SRR5211*_reads.txt > ${SEQ_B73_MO17_SRNA_DIR}/GSM958940_Mo17_smrna_repl3_reads.txt

mkdir -p ${EXP11_PATH}/reads
mv ${SEQ_B73_MO17_SRNA_DIR}/*_reads.txt ${EXP11_PATH}/reads/

## clustering of redundant reads
R --no-save --args "${EXP11_PATH}/reads/" < ${TOOL_CLUSTER_READS}

mkdir -p ${EXP11_PATH}/read_counts
mv ${EXP11_PATH}/reads/*_clustered_reads.txt ${EXP11_PATH}/read_counts/

## merging of read counts
java -jar ${TOOL_MERGE_SEQUENCING_DATASETS} -directory ${EXP11_PATH}/read_counts/ -prefix srna_ -suffix _clustered_reads -fileEnding txt

## quantiles normalization
mkdir -p ${EXP11_PATH}/quantile_normalization_read_counts

java -jar ${TOOL_QUANTILE_NORMALIZATION} -fileName ${EXP11_PATH}/read_counts/merged_sequence_count_data.csv -directory ${EXP11_PATH}/quantile_normalization_read_counts/
java -jar ${TOOL_MERGE_SEQUENCING_DATASETS} -directory ${EXP11_PATH}/quantile_normalization_read_counts/ -prefix srna_ -suffix _qnorm -fileEnding _qnorm.csv

## per million reads scaling of quantile normalized read counts
mkdir -p ${EXP11_PATH}/per_million_quantile_normalization_read_counts

java -jar ${TOOL_PER_MILLION_SCALING} -outputDirectory ${EXP11_PATH}/per_million_quantile_normalization_read_counts/ -fileName ${EXP11_PATH}/quantile_normalization_read_counts/merged_sequence_count_data.csv

## generate sRNA fasta file for B73/Mo17 data
mkdir -p ${EXP11_PATH}/b73_mo17_srna_fasta

java -jar ${TOOL_EXPRESSION_TO_FASTA} -sequence ${EXP11_PATH}/quantile_normalization_read_counts/merged_sequence_count_data.csv > ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta

## extraction of ha-sRNAs from B73/Mo17 sRNA sequences
mkdir -p ${EXP11_PATH}/b73_mo17_hasrna

java -jar ${TOOL_FASTA_MATCH_EXTRACT} -reference ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta -expression ${EXP11_PATH}/quantile_normalization_read_counts/merged_sequence_count_data.csv | awk '{print $1}' > ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.csv
java -jar ${TOOL_FASTA_MATCH_EXTRACT} -reference ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta -expression ${EXP11_PATH}/quantile_normalization_read_counts/merged_sequence_count_data.csv | awk '{print $1}' > ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.csv

java -jar ${TOOL_FASTA_MATCH_SEQUENCE_EXTRACT} -referenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta -sequences ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.csv > ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.fasta
java -jar ${TOOL_FASTA_MATCH_SEQUENCE_EXTRACT} -referenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta -sequences ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.csv > ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.fasta
