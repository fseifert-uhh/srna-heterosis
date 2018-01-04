package smallrnaclustergeneration;

public class SmallRNACluster {
    public SmallRNACluster(int clusterWindowLength, int clusterMinSmallRNACount) {
        this.clusterMinSmallRNACount = clusterMinSmallRNACount;
        this.clusterWindowLength = clusterWindowLength;
    }
    
    public String addSmallRNA(String sequenceTitle, String chromosome, int startPosition, int smallRNALength, String sequenceStrand) {
        boolean newCluster = true;
        
        if((clusterChromosome[strandIndex(sequenceStrand)] != null) && (chromosome.equals(clusterChromosome[strandIndex(sequenceStrand)]))) {
            if(startPosition <= (clusterEnd[strandIndex(sequenceStrand)] + clusterWindowLength)) {
                clusterSmallRNACount[strandIndex(sequenceStrand)]++;
                
                if(clusterEnd[strandIndex(sequenceStrand)] < (startPosition + smallRNALength)) {
                    clusterEnd[strandIndex(sequenceStrand)] = (startPosition + smallRNALength);
                }
                
                clusterSequenceStringBuilder[strandIndex(sequenceStrand)].append(sequenceTitle + ";");
                        
                newCluster = false;
            }
        }
        
        if(newCluster) {
            String clusterDatasetOutput = null;
            if((clusterChromosome[strandIndex(sequenceStrand)] != null) && (clusterSmallRNACount[strandIndex(sequenceStrand)] >= clusterMinSmallRNACount)) {
                clusterDatasetOutput = clusterIndex + "\t" + clusterChromosome[strandIndex(sequenceStrand)] + "\t" + sequenceStrand + "\t" + clusterStart[strandIndex(sequenceStrand)] + "\t" + clusterEnd[strandIndex(sequenceStrand)] + "\t" + clusterSequenceStringBuilder[strandIndex(sequenceStrand)].toString();
            }

            clusterChromosome[strandIndex(sequenceStrand)] = chromosome;
            clusterStart[strandIndex(sequenceStrand)] = startPosition;
            clusterEnd[strandIndex(sequenceStrand)] = (startPosition + smallRNALength);
            clusterSmallRNACount[strandIndex(sequenceStrand)] = 1;
            clusterSequenceStringBuilder[strandIndex(sequenceStrand)] = new StringBuilder(";" + sequenceTitle + ";");
            clusterIndex++;
            
            return clusterDatasetOutput;
        }
        
        return null;
    }
    
    private int strandIndex(String sequenceStrand) {
        if(sequenceStrand.equals("plus")) {
            return 0;
        }
        
        return 1;
    }

    private int clusterIndex = 0;
    private int clusterWindowLength;
    private int clusterMinSmallRNACount;
    
    private int[] clusterStart = new int[2];
    private int[] clusterEnd = new int[2];
    private int[] clusterSmallRNACount = new int[2];
    
    private StringBuilder[] clusterSequenceStringBuilder = new StringBuilder[2];
    
    private String[] clusterChromosome = new String[2];

    private enum SequenceStrand {PLUS, MINUS};
}


