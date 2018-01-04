package illuminareadpreprocess;

public class SequenceQualityTrim {
    public SequenceQualityTrim(String sequence, String qualitySequence, double accuracyThreshold, int phredOffset) {
        this.accuracyThreshold = accuracyThreshold;
        
        this.qualitySequence = qualitySequence;
        this.sequence = sequence;
        
        this.phredOffset = phredOffset;
        this.qualityTrim5Prime = -1;
        this.qualityTrim3Prime = qualitySequence.length();
        
        qualityTrim();
    }
        
    public double getBaseCallAccuracyAt(int position) {
        return (100 - (getIncorrectBaseProbabilityAt(position) * 100));
    }
    
    public double getIncorrectBaseProbabilityAt(int position) {
        return Math.pow(10, (-0.1 * getPhredScoreAt(position)));
    }

    public int getPhredScoreAt(int position) {
        return (qualitySequence.charAt(position) - phredOffset);
    }
    
    public int getQuality5Prime() {
        return (qualityTrim5Prime + 1);
    }
    
    public int getQuality3Prime() {
        return (qualityTrim3Prime - 1);
    }
    
    public String getQualityTrimmedSequence() {
        if((qualityTrim5Prime + 1) < qualityTrim3Prime) {
            return sequence.substring((qualityTrim5Prime + 1), qualityTrim3Prime);
        }
        
        return "";
    }
    
    private void qualityTrim() {
        for(int i = 0, j = sequence.length(); i < j; i++, j--) {
            if((this.getBaseCallAccuracyAt(i) < accuracyThreshold) || (sequence.charAt(i) == 'N')) {
                qualityTrim5Prime = i;
            }
            
            if((this.getBaseCallAccuracyAt(j - 1) < accuracyThreshold) || (sequence.charAt(j - 1) == 'N')) {
                qualityTrim3Prime = j;
            }
        }
    }
    
    private final double accuracyThreshold;
    
    private final int phredOffset;
    private int qualityTrim5Prime;
    private int qualityTrim3Prime;
        
    private final String qualitySequence;
    private final String sequence;
}
