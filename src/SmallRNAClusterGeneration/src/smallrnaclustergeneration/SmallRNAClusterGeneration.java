package smallrnaclustergeneration;

import java.io.File;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class SmallRNAClusterGeneration {
    public SmallRNAClusterGeneration(String bamFileName, int windowLength, int minWindowReadCount) {
        this.bamFileName = bamFileName;

        smallRNACluster = new SmallRNACluster(windowLength, minWindowReadCount);
    }
    
    public void generateClusterOutput() {
        File bamFile = new File(bamFileName);
        
        SAMFileReader bamFileReader = new SAMFileReader(bamFile);
        SAMRecordIterator bamRecordIterator = bamFileReader.iterator();
        while(bamRecordIterator.hasNext()) {
            SAMRecord bamRecord = bamRecordIterator.next();
            
            String clusterOutput = smallRNACluster.addSmallRNA(bamRecord.getReadName(), bamRecord.getReferenceName(), bamRecord.getAlignmentStart(), bamRecord.getReadLength(), (bamRecord.getReadNegativeStrandFlag() ? "minus" : "plus"));
            if(clusterOutput != null) {
                System.out.println(clusterOutput);
            }
        }
        bamFileReader.close();
    }
    
    public static void main(String[] args) {
        int minWindowReadCount = 5;
        int windowLength = 200;
        
        String bamFileName = null;
        
        if(args.length == 0) {
            System.out.println("-filename <path/filename>");
            System.out.println("-minWindowReads <minimal number of reads in window>\tdefault: 5");
            System.out.println("-windowWidth <size of window>\tdefault: 200");
            
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("filename")) {
                        bamFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("minWindowReads")) {
                        minWindowReadCount = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("windowLength")) {
                        windowLength = Integer.valueOf(argumentValue);
                    }
                }
            }
            
            if(bamFileName == null) {
                System.out.println("-filename <path/filename>");
                System.out.println("-minWindowReads <minimal number of reads in window>");
                System.out.println("-windowWidth <size of window>");
            
                System.exit(1);
            }
            
            SmallRNAClusterGeneration smallRNAClusterGeneration = new SmallRNAClusterGeneration(bamFileName, windowLength, minWindowReadCount);
            smallRNAClusterGeneration.generateClusterOutput();
        }
    }
    
    private final String bamFileName;
    
    private final SmallRNACluster smallRNACluster;
}
