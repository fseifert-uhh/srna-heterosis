### ha-sRNA fasta export
mkdir -p ${EXP9_PATH}/hasrna-fasta

java -jar ${TOOL_SRNA_SUBSET_FASTA} -fileName ${EXP6_PATH}/association/mph_gy_pos_expr0.5_fc2.csv -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} > ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta
java -jar ${TOOL_SRNA_SUBSET_FASTA} -fileName ${EXP6_PATH}/association/mph_gy_neg_expr0.5_fc2.csv -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} > ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta


### miRNA mapping
mkdir -p ${EXP9_PATH}/mirna/hisat2-index

cat ${MIRNA_FASTA} | gawk '{if ($0 !~/^>/) {gsub(/U/, "T");gsub(/ /, "");} print}' > ${EXP9_PATH}/mirna/hisat2-index/mirna.fasta

hisat2-build -f ${EXP9_PATH}/mirna/hisat2-index/mirna.fasta ${EXP9_PATH}/mirna/hisat2-index/mirna

hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/mirna/hisat2-index/mirna -U ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta > ${EXP9_PATH}/mirna/mph_gy_positive_associated_srna_mirna.sam
hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/mirna/hisat2-index/mirna -U ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta > ${EXP9_PATH}/mirna//mph_gy_negative_associated_srna_mirna.sam

samtools view -F 4 ${EXP9_PATH}/mirna/mph_gy_positive_associated_srna_mirna.sam | grep 21M | sort | uniq -u  | wc -l | awk '{print 21nt: $1}' > ${EXP9_PATH}/mirna/pos_associated_mirna.txt
samtools view -F 4 ${EXP9_PATH}/mirna/mph_gy_positive_associated_srna_mirna.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/mirna/pos_associated_mirna.txt
samtools view -F 4 ${EXP9_PATH}/mirna/mph_gy_positive_associated_srna_mirna.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/mirna/pos_associated_mirna.txt

samtools view -F 4 ${EXP9_PATH}/mirna/mph_gy_negative_associated_srna_mirna.sam | grep 21M | sort | uniq -u  | wc -l | awk '{print 21nt: $1}' > ${EXP9_PATH}/mirna/neg_associated_mirna.txt
samtools view -F 4 ${EXP9_PATH}/mirna/mph_gy_negative_associated_srna_mirna.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/mirna/neg_associated_mirna.txt
samtools view -F 4 ${EXP9_PATH}/mirna/mph_gy_negative_associated_srna_mirna.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/mirna/neg_associated_mirna.txt


### rRNA mapping
mkdir -p ${EXP9_PATH}/rrna_ssu/hisat2-index

cat ${RRNA_SSU_FASTA} | gawk '{if ($0 !~/^>/) {gsub(/U/, "T");gsub(/ /, "");} print}' > ${EXP9_PATH}/rrna_ssu/hisat2-index/rrna_ssu.fasta

hisat2-build -f ${EXP9_PATH}/rrna_ssu/hisat2-index/rrna_ssu.fasta ${EXP9_PATH}/rrna_ssu/hisat2-index/rrna_ssu

hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/rrna_ssu/hisat2-index/rrna_ssu -U ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta > ${EXP9_PATH}/rrna_ssu/mph_gy_positive_associated_srna_rrna_ssu.sam
hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/rrna_ssu/hisat2-index/rrna_ssu -U ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta > ${EXP9_PATH}/rrna_ssu//mph_gy_negative_associated_srna_rrna_ssu.sam

samtools view -F 4 ${EXP9_PATH}/rrna_ssu/mph_gy_positive_associated_srna_rrna_ssu.sam | grep 21M | sort | uniq -u  | wc -l | awk '{print 21nt: $1}' > ${EXP9_PATH}/rrna_ssu/pos_associated_rrna_ssu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_ssu/mph_gy_positive_associated_srna_rrna_ssu.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/rrna_ssu/pos_associated_rrna_ssu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_ssu/mph_gy_positive_associated_srna_rrna_ssu.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/rrna_ssu/pos_associated_rrna_ssu.txt

samtools view -F 4 ${EXP9_PATH}/rrna_ssu/mph_gy_negative_associated_srna_rrna_ssu.sam | grep 21M | sort | uniq -u  | wc -l| awk '{print 21nt: $1}' > ${EXP9_PATH}/rrna_ssu/neg_associated_rrna_ssu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_ssu/mph_gy_negative_associated_srna_rrna_ssu.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/rrna_ssu/neg_associated_rrna_ssu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_ssu/mph_gy_negative_associated_srna_rrna_ssu.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/rrna_ssu/neg_associated_rrna_ssu.txt

mkdir -p ${EXP9_PATH}/rrna_lsu/hisat2-index

cat ${RRNA_LSU_FASTA} | gawk '{if ($0 !~/^>/) {gsub(/U/, "T");gsub(/ /, "");} print}' > ${EXP9_PATH}/rrna_lsu/hisat2-index/rrna_lsu.fasta

hisat2-build -f ${EXP9_PATH}/rrna_lsu/hisat2-index/rrna_lsu.fasta ${EXP9_PATH}/rrna_lsu/hisat2-index/rrna_lsu

hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/rrna_lsu/hisat2-index/rrna_lsu -U ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta > ${EXP9_PATH}/rrna_lsu/mph_gy_positive_associated_srna_rrna_lsu.sam
hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/rrna_lsu/hisat2-index/rrna_lsu -U ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta > ${EXP9_PATH}/rrna_lsu//mph_gy_negative_associated_srna_rrna_lsu.sam

samtools view -F 4 ${EXP9_PATH}/rrna_lsu/mph_gy_positive_associated_srna_rrna_lsu.sam | grep 21M | sort | uniq -u  | wc -l | awk '{print 21nt: $1}' > ${EXP9_PATH}/rrna_lsu/pos_associated_rrna_lsu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_lsu/mph_gy_positive_associated_srna_rrna_lsu.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/rrna_lsu/pos_associated_rrna_lsu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_lsu/mph_gy_positive_associated_srna_rrna_lsu.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/rrna_lsu/pos_associated_rrna_lsu.txt

samtools view -F 4 ${EXP9_PATH}/rrna_lsu/mph_gy_negative_associated_srna_rrna_lsu.sam | grep 21M | sort | uniq -u  | wc -l | awk '{print 21nt: $1}' > ${EXP9_PATH}/rrna_lsu/neg_associated_rrna_lsu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_lsu/mph_gy_negative_associated_srna_rrna_lsu.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/rrna_lsu/neg_associated_rrna_lsu.txt
samtools view -F 4 ${EXP9_PATH}/rrna_lsu/mph_gy_negative_associated_srna_rrna_lsu.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/rrna_lsu/neg_associated_rrna_lsu.txt


### tRNA mapping
mkdir -p ${EXP9_PATH}/trna/hisat2-index

cat ${TRNA_FASTA} | gawk '{if ($0 !~/^>/) {gsub(/U/, "T");gsub(/ /, "");} print}' > ${EXP9_PATH}/trna/hisat2-index/rrna_ssu.fasta

hisat2-build -f ${EXP9_PATH}/trna/hisat2-index/trna.fasta ${EXP9_PATH}/trna/hisat2-index/trna

hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/trna/hisat2-index/trna -U ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta > ${EXP9_PATH}/trna/mph_gy_positive_associated_srna_trna.sam
hisat2 -k1 -p4 --all -f --no-spliced-alignment --sp 1000 --mp 1000 --rdg 1000,1000 --rfg 1000,000 -x ${EXP9_PATH}/trna/hisat2-index/trna -U ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta > ${EXP9_PATH}/trna//mph_gy_negative_associated_srna_trna.sam

samtools view -F 4 ${EXP9_PATH}/trna/mph_gy_positive_associated_srna_trna.sam | grep 21M | sort | uniq -u  | wc -l | awk '{print 21nt: $1}' > ${EXP9_PATH}/trna/pos_associated_trna.txt
samtools view -F 4 ${EXP9_PATH}/trna/mph_gy_positive_associated_srna_trna.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/trna/pos_associated_trna.txt
samtools view -F 4 ${EXP9_PATH}/trna/mph_gy_positive_associated_srna_trna.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/trna/pos_associated_trna.txt

samtools view -F 4 ${EXP9_PATH}/trna/mph_gy_negative_associated_srna_trna.sam | grep 21M | sort | uniq -u  | wc -l | awk '{print 21nt: $1}' > ${EXP9_PATH}/trna/neg_associated_trna.txt
samtools view -F 4 ${EXP9_PATH}/trna/mph_gy_negative_associated_srna_trna.sam | grep 22M | sort | uniq -u  | wc -l | awk '{print 22nt: $1}' >> ${EXP9_PATH}/trna/neg_associated_trna.txt
samtools view -F 4 ${EXP9_PATH}/trna/mph_gy_negative_associated_srna_trna.sam | grep 24M | sort | uniq -u  | wc -l | awk '{print 24nt: $1}' >> ${EXP9_PATH}/trna/neg_associated_trna.txt

