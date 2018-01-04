### sequence mapping
mkdir -p ${EXP3_PATH}/bowtie-index
mkdir -p ${EXP3_PATH}/mapping

bowtie-build ${GENOME_FASTA} ${EXP3_PATH}/bowtie-index/reference
bowtie -p${MAX_THREADS} -a -f -v0 --sam data/bowtie-index/reference ${EXP2_PATH}/srna_fasta/srna_heterosis_reads.fasta | samtools view -Sb > ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.bam
samtools sort --threads ${MAX_THREADS} -o ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.sorted.bam ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.bam
samtools index ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.sorted.bam

### mapping count database import
java -jar ${TOOL_DB_MAPPING_COUNT} -bamMappingFile ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.bam -databaseTable srna_mapping_status -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD}

### annotation/window mapping database import
java -jar ${TOOL_ANNOTATION_CLUSTER_GEN} -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} -bamMappingFilename ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.sorted.bam -gffAnnotationFilename ${EXP2_PATH}/annotation.gff3
java -jar ${TOOL_WINDOW_CLUSTER} -databaseUser ${DATABASE_USER} -databasePassword ${DATABASE_PASSWORD} -bamMappingFilename ${EXP3_PATH}/mapping/srna_heterosis_zmb73_refgen4.sorted.bam
