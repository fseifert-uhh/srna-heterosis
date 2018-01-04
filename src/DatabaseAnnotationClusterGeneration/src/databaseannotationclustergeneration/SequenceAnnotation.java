package databaseannotationclustergeneration;

public class SequenceAnnotation {
    public SequenceAnnotation(int index, String chromosome, SequenceAnnotationType sequenceAnnotationType, int startPosition, int endPosition, String strand) {
        this.chromosome = chromosome;
        this.endPosition = endPosition;
        this.index = index;
        this.sequenceAnnotationType = sequenceAnnotationType;
        this.startPosition = startPosition;
        this.strand = strand;
    }
    
    public String getChromosome() {
        return chromosome;
    }
    
    public int getEndPosition()  {
        return endPosition;
    }
    
    public int getIndex() {
        return index;
    }
    
    public SequenceAnnotationType getSequenceAnnotationType() {
        return sequenceAnnotationType;
    }
    
    public int getStartPosition()  {
        return startPosition;
    }
    
    public String getStrand() {
        return strand;
    }
    
    private int endPosition;
    private int index;
    private int startPosition;
    
    private String chromosome;
    private String strand;

    private SequenceAnnotationType sequenceAnnotationType;
}
