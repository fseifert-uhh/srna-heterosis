package de.uni_hamburg.fseifert.fastq;

public class FastQDataset {
    public FastQDataset(String title, String sequence, String quality) {
        this.title = title;
        this.nucleotideSequence = sequence;
        this.qualitySequence = quality;
    }
    
    public char nucleotideAt(int position) {
        return nucleotideSequence.charAt(position);
    }
    
    public String getQualitySequence() {
        return qualitySequence;
    }
    
    public String getSequence() {
        return nucleotideSequence;
    }
    
    public String getTitle() {
        return title;
    }
    
    private String title;
    private String nucleotideSequence;
    private String qualitySequence;
}
