### Regulski et al. 2013 - B73/Mo17 mRNA sequencing data processing

# sequencing data retrieval
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958906_B73_rep1 SRR520998
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958906_B73_rep1 SRR520999
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958908_B73_rep2 SRR521003 
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958911_B73_rep3 SRR521006
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958907_Mo17_rep1 SRR521000
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958907_Mo17_rep1 SRR521001 
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958907_Mo17_rep1 SRR521002
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958909_Mo17_rep2 SRR521004
fastq-dump --split-3 -O ${SEQ_B73_MO17_SRNA_DIR}/GSM958910_Mo17_rep3 SRR521005

# sequence mapping
mkdir -p ${EXP12_PATH}/bowtie2_index

bowtie2-build -q ${GENOME_FASTA} ${EXP12_PATH}/bowtie2_index/reference

mkdir -p ${EXP12_PATH}/tophat2_mapping
tophat -o ${EXP12_PATH}/tophat2_mapping/GSM958906_B73_rep1 ${EXP12_PATH}/bowtie2_index/reference ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958906_B73_rep1/SRR520998_1.fastq,${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958906_B73_rep1/SRR520999_1.fastq ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958906_B73_rep1/SRR520998_2.fastq,${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958906_B73_rep1/SRR520999_2.fastq
tophat -o ${EXP12_PATH}/tophat2_mapping/GSM958908_B73_rep2 ${EXP12_PATH}/bowtie2_index/reference ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958908_B73_rep2/SRR521003_1.fastq ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958908_B73_rep2/SRR521003_2.fastq
tophat -o ${EXP12_PATH}/tophat2_mapping/GSM958911_B73_rep3 ${EXP12_PATH}/bowtie2_index/reference ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958911_B73_rep3/SRR521006_1.fastq ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958911_B73_rep3/SRR521006_2.fastq
tophat -o ${EXP12_PATH}/tophat2_mapping/GSM958907_Mo17_rep1 ${EXP12_PATH}/bowtie2_index/reference ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958907_Mo17_rep1/SRR521000_1.fastq,${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958907_Mo17_rep1/SRR521001_1.fastq,${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958907_Mo17_rep1/SRR521002_1.fastq ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958907_Mo17_rep1/SRR521000_2.fastq,${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958907_Mo17_rep1/SRR521001_2.fastq,${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958907_Mo17_rep1/SRR521002_2.fastq
tophat -o ${EXP12_PATH}/tophat2_mapping/GSM958909_Mo17_rep2 ${EXP12_PATH}/bowtie2_index/reference ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958909_Mo17_rep2/SRR521004_1.fastq ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958909_Mo17_rep2/SRR521004_2.fastq
tophat -o ${EXP12_PATH}/tophat2_mapping/GSM958910_Mo17_rep3 ${EXP12_PATH}/bowtie2_index/reference ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958910_Mo17_rep3/SRR521005_1.fastq ${SEQ_B73_MO17_TRANSCRIPTOME_DIR}/GSM958910_Mo17_rep3/SRR521005_2.fastq

mkdir -p ${EXP12_PATH}/cufflinks
cufflinks -q -g ${GENE_GFF3} -o ${EXP12_PATH}/cufflinks/GSM958906_B73_rep1 ${EXP12_PATH}/tophat2_mapping/GSM958906_B73_rep1/accepted_hits.bam
cufflinks -q -g ${GENE_GFF3} -o ${EXP12_PATH}/cufflinks/GSM958908_B73_rep2 ${EXP12_PATH}/tophat2_mapping/GSM958908_B73_rep2/accepted_hits.bam
cufflinks -q -g ${GENE_GFF3} -o ${EXP12_PATH}/cufflinks/GSM958911_B73_rep3 ${EXP12_PATH}/tophat2_mapping/GSM958911_B73_rep3/accepted_hits.bam
cufflinks -q -g ${GENE_GFF3} -o ${EXP12_PATH}/cufflinks/GSM958907_Mo17_rep1 ${EXP12_PATH}/tophat2_mapping/GSM958907_Mo17_rep1/accepted_hits.bam
cufflinks -q -g ${GENE_GFF3} -o ${EXP12_PATH}/cufflinks/GSM958909_Mo17_rep2 ${EXP12_PATH}/tophat2_mapping/GSM958909_Mo17_rep2/accepted_hits.bam
cufflinks -q -g ${GENE_GFF3} -o ${EXP12_PATH}/cufflinks/GSM958910_Mo17_rep3 ${EXP12_PATH}/tophat2_mapping/GSM958910_Mo17_rep3/accepted_hits.bam

mkdir -p ${EXP12_PATH}/cuffmerge
cuffmerge -g ${GENE_GFF3} -s ${GENOME_FASTA} ${SEQ_B73_TRANSCRIPTOME_DIR}/assemblies.txt
mv merged_asm ${EXP12_PATH}/cuffmerge

mkdir -p ${EXP12_PATH}/cuffdiff
cuffdiff -q -FDR 0.1 -o ${EXP12_PATH}/cuffdiff -b ${GENOME_FASTA} -L B73,Mo17 -u ${EXP12_PATH}/cuffmerge/merged_asm/merged.gtf ${EXP12_PATH}/tophat2_mapping/GSM958906_B73_rep1/accepted_hits.bam,${EXP12_PATH}/tophat2_mapping/GSM958908_B73_rep2/accepted_hits.bam,${EXP12_PATH}/tophat2_mapping/GSM958911_B73_rep3/accepted_hits.bam ${EXP12_PATH}/tophat2_mapping/GSM958907_Mo17_rep1/accepted_hits.bam,${EXP12_PATH}/tophat2_mapping/GSM958909_Mo17_rep2/accepted_hits.bam,${EXP12_PATH}/tophat2_mapping/GSM958910_Mo17_rep3/accepted_hits.bam

# extract DE features
awk '{if($14=="yes") print $0}' ${EXP12_PATH}/cuffdiff/gene_exp.diff > ${EXP12_PATH}/cuffdiff/gene_exp_de.diff
cat gene_exp_de.diff | awk '{if(length($3)>1) {gsub(/gene:/, ""); gsub(/,/, "\n"); print $3}}' | sort | uniq > ${EXP12_PATH}/cuffdiff/de_genes.txt

awk '{if($14=="yes") print $0}' ${EXP12_PATH}/cuffdiff/promoters.diff > ${EXP12_PATH}/cuffdiff/promoters_de.diff
cat promoters_de.diff | awk '{if(length($3)>1) {gsub(/gene:/, ""); gsub(/,/, "\n"); print $3}}' | sort | uniq > de_promoter.txt

awk '{if($14=="yes") print $0}' ${EXP12_PATH}/cuffdiff/splicing.diff > ${EXP12_PATH}/cuffdiff/splicing_de.diff
cat splicing_de.diff | awk '{if(length($3)>1) {gsub(/gene:/, ""); gsub(/,/, "\n"); print $3}}' | sort | uniq > ${EXP12_PATH}/cuffdiff/de_splicing.txt

