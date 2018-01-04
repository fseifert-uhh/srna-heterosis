# srna-heterosis

This project contains all scripts and Java programs used for bioinformatics analyses in the publication:

> Seifert _et al._ (2018) Parental expression variation of small RNAs is negatively correlated with grain yield heterosis in a maize breeding population. Frontiers in Plant Science, doi: 10.3389/fpls.2018.00013 

## Prerequisites
### Software
* bowtie ([github](https://github.com/BenLangmead/bowtie))
* bowtie2 ([github](https://github.com/BenLangmead/bowtie))
* cufflinks ([github](http://cole-trapnell-lab.github.io/cufflinks))
* Java Runtime Environment (JRE) 1.7 or higher
* sra-tools ([github](https://github.com/ncbi/sra-tools))
* hisat2 ([website](https://ccb.jhu.edu/software/hisat2/index.shtml)) 
* R ([website](https://www.r-project.org))
* samtools ([sourceforge](http://samtools.sourceforge.net/))
* tophat ([website](https://ccb.jhu.edu/software/tophat/manual.shtml))


### Data (Genome & Annotation)
* The genome needs to be downloaded and decompressed into the folder data/genome/ ([Download genome (FASTA): Zea mays AGPv4](ftp://ftp.gramene.org/pub/gramene/archives/PAST_RELEASES/release-54/fasta/zea_mays/dna/Zea_mays.AGPv4.dna.toplevel.fa.gz))
* The gene annotation needs to be downloaded and decompressed into the folder data/annotation/ ([Download gene annotation (GFF3): Zea mays AGPv4.36](ftp://ftp.gramene.org/pub/gramene/archives/PAST_RELEASES/release-54/gff3/zea_mays/Zea_mays.AGPv4.36.chr.gff3.gz))
* The repeat annotation needs to be downloaded and decompressed into the folder data/annotation/ ([Download repeat annotation (GFF3): Zea mays B74v4](ftp://ftp.gramene.org/pub/gramene/archives/PAST_RELEASES/release-54/gff3/zea_mays/repeat_annotation/B73v4.TE.filtered.gff3.gz)) 

* Maize mature miRNA sequences need to be downloaded in FASTA format from ([miRBase](http://www.mirbase.org/)), and saved in the folder data/sequences/mirna/. Replace the text "XYZ.fasta" in the variable MIRNA_FASTA in file runall_000_init.sh with the file name 
* Maize rRNA SSU and rRNA LSU sequences need to be downloaded in FASTA format from ([silva])(https://www.arb-silva.de/)), and saved in the folder data/sequences/rrna/. Replace the text "XYZ.fasta" in the variable RRNA_FASTA  in file runall_000_init.sh with the file name 
* Maize tRNA sequences need to be downloaded in FASTA format from ([GtRNAdb](http://http://gtrnadb.ucsc.edu/)), and saved in the folder data/sequences/trna/. Replace the text "XYZ.fasta" in the variable TRNA_FASTA in file runall_000_init.sh with the file name 


### Java/R sources
All sources for programs/scripts used for the analyses can be found in the folder src/ subsequent folders are NetBeans Projects ([NetBeans website](https://netbeans.org/downloads/))


### Execution

The shell scripts (runall_xyz.sh) were run on Debian Linux in bash. The files need the execution permission (chmod 755).
The scripts have to be run consecutively following their numbering. File runall_000_init.sh contains variable 
definitions and needs to be rerun, in case the computer has been rebooted.
Some analyses depend on previous results and wont be executed if the previous analyses have not been performed
