package de.uni_hamburg.fseifert.gff;

public class GFFDataset {
    public GFFDataset(String sequenceId, String source, String sequenceType, long sequenceStart, long sequenceEnd, double score, char strand, byte phase, String attributes) {
        gffSequenceId = sequenceId;
        gffSource = source;
        gffSequenceType = sequenceType;
        gffSequenceStart = sequenceStart;
        gffSequenceEnd = sequenceEnd;
        gffScore = score;
        gffSequenceStrand = strand;
        gffSequencePhase = phase;
        gffAttributes = attributes;
        gffLine = gffSequenceId + "\t" + (gffSource.isEmpty() ? "." : gffSource) + "\t" + gffSequenceType + "\t" + gffSequenceStart + "\t" + gffSequenceEnd + "\t" + ((gffScore > 0) ? gffScore : ".") + "\t" + gffSequenceStrand + "\t" + ((gffSequencePhase > 0) ? gffSequencePhase : ".") + "\t" + (gffAttributes.isEmpty() ? "." : gffAttributes);
    }
    
    public GFFDataset(String gffLine) {
        this.gffLine = gffLine;
        if(gffLine != null) {
            gffData = gffLine.split("\t");
            if(gffData.length == 9) {
                gffSequenceId = gffData[0];
                gffSource = gffData[1];
                gffSequenceType = gffData[2];
                gffSequenceStart = Long.valueOf(gffData[3]);
                gffSequenceEnd = Long.valueOf(gffData[4]);

                if(gffData[5].charAt(0) != gffSpaceholder) {
                    gffScore = Double.valueOf(gffData[5]);
                }
                else {
                    gffScore = 0;
                }

                gffSequenceStrand = gffData[6].charAt(0);

                if(gffData[7].charAt(0) != gffSpaceholder) {
                    gffSequencePhase = Byte.valueOf(gffData[7]);
                }
                else {
                    gffSequencePhase = 4;
                }

                gffAttributes = gffData[8];
            }
        }
    }
    
    public String getAttributes() {
        return gffAttributes;
    }

    public String getGFFString() {
        return gffLine;
    }
    
    public double getScore() {
        return gffScore;
    }
    
    public long getSequenceEnd() {
        return gffSequenceEnd;
    }

    public String getSequenceId() {
        return gffSequenceId;
    }

    public long getSequenceLength() {
        return ((gffSequenceEnd - gffSequenceStart) + 1);
    }

    public long getSequenceStart() {
        return gffSequenceStart;
    }
    
    public char getSequenceStrand() {
        return gffSequenceStrand;
    }

    public String getSequenceType() {
        return gffSequenceType;
    }
    
    public String getSource() {
        return gffSource;
    }

    private char gffSpaceholder = '.';
    
    private String gffLine;
    private String[] gffData;
    
    private String gffSequenceId;
    private String gffSource;
    private String gffSequenceType;
    private long gffSequenceStart;
    private long gffSequenceEnd;
    private double gffScore;
    private char gffSequenceStrand;
    private byte gffSequencePhase;
    private String gffAttributes;
}
