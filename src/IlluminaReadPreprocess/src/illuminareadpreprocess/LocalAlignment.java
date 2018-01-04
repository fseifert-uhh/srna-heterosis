package illuminareadpreprocess;

public class LocalAlignment {
    public LocalAlignment(String sequence1, String sequence2) {
        this.sequence1 = sequence1.toLowerCase();
        this.sequence2 = sequence2.toLowerCase();
        
        dynamicProgrammingTable = new int[sequence1.length() + 1][sequence2.length() + 1];
        calculateAlignment();
        tracebackAlignment();
    }
    
    public int alignmentStartPosition() {
        return tracebackPositionSequence1;
    }   

    public int alignmentEndPosition() {
        return (maximumPositionSequence1);
    }   

    private void calculateAlignment() {
        for(int i = 1; i <= sequence1.length(); i++) {
            for(int j = 1; j < sequence2.length(); j++) {
                deletionScore = dynamicProgrammingTable[i - 1][j] + deletionCost;
                insertionScore = dynamicProgrammingTable[i][j - 1] + insertionCost;
                replacementScore = dynamicProgrammingTable[i - 1][j - 1] + (sequenceMatch(sequence1.charAt(i - 1), sequence2.charAt(j - 1)) ? replacementMatchCost : replacementMismatchCost);

                dynamicProgrammingTable[i][j] = Math.max(Math.max(0, deletionScore), Math.max(insertionScore, replacementScore));                
                if(dynamicProgrammingTable[i][j] > maximumScore) {
                    maximumScore = dynamicProgrammingTable[i][j];
                    maximumPositionSequence1 = i;
                    maximumPositionSequence2 = j;
                }
            }
        }
    }
    
    private int max(int a, int b) {
        if(a > b) {
            return a;
        }
        
        return b;
    }
    
    private boolean sequenceMatch(char nucleotide1, char nucleotide2) {
        if(nucleotide1 == nucleotide2) {
            return true;
        }
        
        return false;
    }
    
    private void tracebackAlignment() {
        int i = maximumPositionSequence1;
        int j = maximumPositionSequence2;
        
        if(maximumScore > 0) {
            while((dynamicProgrammingTable[i][j] > 0) && (i > 0) && (j > 0)) {
                if(dynamicProgrammingTable[i][j] == (dynamicProgrammingTable[i - 1][j - 1] + replacementMatchCost)) {
                    i--;
                    j--;
                }
                else if(dynamicProgrammingTable[i][j] == (dynamicProgrammingTable[i - 1][j - 1] + replacementMismatchCost)) {
                    i--;
                    j--;
                }
                else if(dynamicProgrammingTable[i][j] == (dynamicProgrammingTable[i][j - 1] + insertionCost)) {
                    j--;
                }
                else {
                    i--;
                }
            }
        }
        
        tracebackPositionSequence1 = i;
    }
    
    private final int[][] dynamicProgrammingTable;
    
    private final int deletionCost = -1;
    private final int insertionCost = -1;
    private final int replacementMatchCost = 2;
    private final int replacementMismatchCost = -1;
    
    private int deletionScore = 0;
    private int insertionScore = 0;
    private int replacementScore = 0;
    
    private int maximumPositionSequence1 = -1;
    private int maximumPositionSequence2 = -1;
    private int maximumScore = 0;
    
    private int tracebackPositionSequence1 = 0;
    
    private final String sequence1;
    private final String sequence2;
}
