args <- commandArgs(trailingOnly = TRUE)
path <- args[1]

full_path <- paste(paste(getwd(), "/", sep=""), path, sep="")

files <- list.files(full_path, pattern="_reads.txt", all.files=T, full.names=T)
for (sequence_file in files) {
	sequence_file
	
	sequence_read_data <- read.table(sequence_file)
	clustered_sequence_read_data <- as.data.frame(table(sequence_read_data))
	write.table(clustered_sequence_read_data, file=sub("_reads\\.txt$","_clustered_reads.txt", sequence_file), sep="\t", col.names=F, row.names=F, quote=F)
}
