### DE Gene ha-sRNA enrichment

mkdir -p ${EXP13_PATH}

# flint/dent lines
mkdir -p ${EXP13_PATH}/flint_dent

java -jar ${TOOL_GENE_REGION_GFF} -transcripts data/microarray/de_genes_flint_dent.csv -referenceGFF ${GENE_GFF3} -upstreamWindow 1000 -downstreamWindow 1000 > ${EXP13_PATH}/flint_dent/de_genes.gff 

java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/flint_dent/de_genes.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta -sequenceFasta ${EXP2_PATH}/srna_fasta/srna_heterosis_reads.fasta > ${EXP13_PATH}/flint_dent/22nt_pos_hasrna_flint_dent_genes.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/flint_dent/de_genes.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP9_PATH}/hasrna-fasta/mph_gy_pos_expr0.5_fc2.fasta -sequenceFasta ${EXP2_PATH}/srna_fasta/srna_heterosis_reads.fasta > ${EXP13_PATH}/flint_dent/24nt_pos_hasrna_flint_dent_genes.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/flint_dent/de_genes.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta -sequenceFasta ${EXP2_PATH}/srna_fasta/srna_heterosis_reads.fasta > ${EXP13_PATH}/flint_dent/22nt_neg_hasrna_flint_dent_genes.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/flint_dent/de_genes.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP9_PATH}/hasrna-fasta/mph_gy_neg_expr0.5_fc2.fasta -sequenceFasta ${EXP2_PATH}/srna_fasta/srna_heterosis_reads.fasta > ${EXP13_PATH}/flint_dent/24nt_neg_hasrna_flint_dent_genes.csv

# B73/Mo17
mkdir -p ${EXP13_PATH}/b73_mo17/genes

java -jar ${TOOL_GENE_REGION_GFF} -transcripts ${EXP12_PATH}/cuffdiff/de_genes.txt -referenceGFF ${GENE_GFF3} -upstreamWindow 1000 -downstreamWindow 1000 > ${EXP13_PATH}/b73_mo17/de_genes.gff 

java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_genes.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/genes/22nt_pos_hasrna_b73_mo17_genes.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_genes.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/genes/24nt_pos_hasrna_b73_mo17_genes.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_genes.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/genes/22nt_neg_hasrna_b73_mo17_genes.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_genes.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/genes/24nt_neg_hasrna_b73_mo17_genes.csv

mkdir -p ${EXP13_PATH}/b73_mo17/splicing

java -jar ${TOOL_GENE_REGION_GFF} -transcripts ${EXP12_PATH}/cuffdiff/de_splicing.txt -referenceGFF ${GENE_GFF3} -upstreamWindow 1000 -downstreamWindow 1000 > ${EXP13_PATH}/b73_mo17/de_splicing.gff 

java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_splicing.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/splicing/22nt_pos_hasrna_b73_mo17_splicing.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_splicing.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/splicing/24nt_pos_hasrna_b73_mo17_splicing.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_splicing.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/splicing/22nt_neg_hasrna_b73_mo17_splicing.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_splicing.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/splicing/24nt_neg_hasrna_b73_mo17_splicing.csv

mkdir -p ${EXP13_PATH}/b73_mo17/promoter

java -jar ${TOOL_GENE_REGION_GFF} -transcripts ${EXP12_PATH}/cuffdiff/de_promoter.txt -referenceGFF ${GENE_GFF3} -upstreamWindow 1000 -downstreamWindow 1000 > ${EXP13_PATH}/b73_mo17/de_promoter.gff 

java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_promoter.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/promoter/22nt_pos_hasrna_b73_mo17_promoter.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_promoter.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_pos_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/promoter/24nt_pos_hasrna_b73_mo17_promoter.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_promoter.gff -minLength 22 -maxLength 22 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/promoter/22nt_neg_hasrna_b73_mo17_promoter.csv
java -jar ${TOOL_HASRNA_REGION_ENRICHMENT} -bowtieIndex ${EXP3_PATH}/bowtie-index/reference -lociGff ${EXP13_PATH}/b73_mo17/de_promoter.gff -minLength 24 -maxLength 24 -referenceFasta ${EXP11_PATH}/b73_mo17_hasrna/mph_gy_neg_srna_b73_mo17.fasta -sequenceFasta ${EXP11_PATH}/b73_mo17_srna_fasta/regulski_srna_numbered.fasta > ${EXP13_PATH}/b73_mo17/promoter/24nt_neg_hasrna_b73_mo17_promoter.csv
