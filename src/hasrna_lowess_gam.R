install.packages("gam")
library(gam)

chromosome_length <- c(307, 244, 236, 247, 224, 174, 182, 181, 155, 151)

position <- c() #init

for(chromosome in 1:10) {
	data <- read.table(file=paste("data/genetic3_map/chr", chromosome, "_map.csv", sep=""), sep="\t", header=F)
	lowess_data <- lowess(data$V1, data$V2, f=0.1, iter=1000)
	fitted_lo_gam<-gam(y ~ s(x), data=lowess_data)

	position$x = seq(0,chromosome_length[chromosome]*1000000, by=1000000)
	map <- predict.gam(fitted_lo_gam,position)

	write.table(map, file=paste("010_recombination_rate/chr", chromosome, "_map_lowess.csv", sep=""), sep="\t", col.names=F, row.names=F)
}