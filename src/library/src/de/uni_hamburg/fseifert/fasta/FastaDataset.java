package de.uni_hamburg.fseifert.fasta;

public class FastaDataset {
    public FastaDataset(String title, String sequence) {
        this.sequence = sequence;
        this.title = title;
    }
    
    public String getSequence() {
        return sequence;
    }

    public String getTitle() {
        return title;
    }
    
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String sequence;
    private String title;
}
