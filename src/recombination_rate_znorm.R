options(scipen = 18)

for(chromosome in 1:10) {
	data <- read.table(file=paste("010_recombination_rate/chr", chromosome, "_map_lowess.csv", sep=""), sep="\t", header=F)
	
	recombination_rate = array()
	chromosome_title=array()
	window_start = array()
	window_end = array()
	
	for(index in 1:length(data[,1])) {
		chromosome_title[index] = paste("chr", chromosome, sep="")
		window_start[index] = paste((index - 1) * 1000000)
		window_end[index] = ((index * 1000000) - 1)
		
		if(index == 1) {
			recombination_rate[1] = 0
		}
		else {
			recombination_rate[index] = (data[index,1] - data[index - 1,1])
		}
	}

	recombination_rate_znorm = scale(recombination_rate, center=TRUE, scale=TRUE)
	
	output_data = data.frame(chromosome_title, window_start, window_end, recombination_rate_znorm)
	
	write.table(format(output_data, scientific=F), file=paste("010_recombination_rate/chr", chromosome, "_map_lowess_znorm.csv", sep=""), sep="\t", row.names=F, col.names=F, quote=F)
}
