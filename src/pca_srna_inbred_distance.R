data <- read.delim("004_srna_population_diversity/srna_inbred_distance/srna_inbred_distance/srna_inbred_binary_distance_expr0_5_fc2.csv", sep="\t", header=T)
rownames(data)<-colnames(data)
data<-as.matrix(data[,-1])

data.pca <- princomp(data, scale=F, cor=F)
write.table(data.pca$scores, file="004_srna_population_diversity/srna_inbred_distance/srna_inbred_pca-scores.csv", quote=F, col.names=F, sep="\t")

library(FactoMineR)
pca.result <- PCA(data, graph=F)

svg("004_srna_population_diversity/srna_inbred_distance/srna_de_pca_germplasm_grouping_expr0_5_fc2.svg")
par(mar=c(3,3,0.5,0.5),cex=1.5)
plot(pca.result, bty="n", ylim=c(-4,4), title="", cex=0.9, col.ind=c("dodgerblue2", "dodgerblue2", "dodgerblue2", "dodgerblue2", "dodgerblue4", "dodgerblue4", "dodgerblue4", "firebrick2", "firebrick2", "firebrick2", "firebrick2", "firebrick2", "firebrick2", "firebrick4", "firebrick4", "firebrick4", "firebrick4", "firebrick4", "firebrick4", "firebrick4", "firebrick4"), label="ind", col.lab="white", cex.axis=0.95, mgp=c(2,0.5,0), family="Liberation Sans", xlim=c(-4,4))
title(xlab="PC 1 (15.66 %)", ylab="PC 2 (10.39 %)", mgp=c(2,0.5,0), cex=0.7, family="Liberation Sans")
dev.off();