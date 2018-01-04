package bootstrapsmallrnaregionalenrichmentanalysis;

import de.uni_hamburg.fseifert.gff.GFFDataset;
import de.uni_hamburg.fseifert.gff.GFFParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class BootstrapMappedSmallRNARegionalEnrichmentAnalysis {
    public BootstrapMappedSmallRNARegionalEnrichmentAnalysis(String referenceFastaFileName, String sequenceFastaFileName, int minLength, int maxLength, int bootstrapNumber) throws IOException {
        this.bootstrapNumber = bootstrapNumber;
        this.minLength = minLength;
        this.maxLength = maxLength;
        
        this.sequenceFastaFileName = sequenceFastaFileName;
        
        referenceLengthBootstrapDistribution = new ReferenceLengthBootstrapDistribution(sequenceFastaFileName, minLength, maxLength);
        referenceLengthBootstrapDistribution.setReferenceDistribution(referenceFastaFileName);
        referenceLengthBootstrapDistribution.generateBootstrapDistribution(bootstrapNumber);
    }
    
    public void calculateBootstrapProbability(String bamMappingFileName, String lociGFFFileName) throws IOException, InterruptedException {
        SAMFileReader bamFileReader = new SAMFileReader(new File(bamMappingFileName));
        bamFileReader = new SAMFileReader(new File(bamMappingFileName));

        System.out.println("annotation\tchromosome\tstart\tend\tstrand\treference_count\taverage_bootstrap_count\tenrichment_probability\t\tattributes");
        
        GFFParser gffParser = new GFFParser(lociGFFFileName);
        while(gffParser.hasNext()) {
            GFFDataset gffDataset = gffParser.next();
            
            int[] sequenceCount = new int[bootstrapNumber + 1];
            
            SAMRecordIterator bamRecordIterator = bamFileReader.query(gffDataset.getSequenceId(), (int) gffDataset.getSequenceStart(), (int) gffDataset.getSequenceEnd(), false);
            while(bamRecordIterator.hasNext()) {
                SAMRecord bamRecord = bamRecordIterator.next();
                
                int sequenceIndex = Integer.valueOf(bamRecord.getReadName());
                
                ArrayList<Integer> sequenceBootstrapDistributionArrayList = referenceLengthBootstrapDistribution.getBootstrapSequencePresence(sequenceIndex);
                if(sequenceBootstrapDistributionArrayList != null) {
                    for(int bootstrapIndex : sequenceBootstrapDistributionArrayList) {
                        sequenceCount[bootstrapIndex]++;
                    }
                }
            }
            bamRecordIterator.close();
            
            if(sequenceCount[0] > 0) {
                double averageBootstrapSequenceCount = 0;
                double enrichmentProbability = 0;

                for(int bootstrapIndex = 1; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                    averageBootstrapSequenceCount += (double) sequenceCount[bootstrapIndex];
                    
                    if(sequenceCount[0] <= sequenceCount[bootstrapIndex]) {
                        enrichmentProbability += (1.0 / (double) bootstrapNumber);
                    }
                }
                averageBootstrapSequenceCount /= (double) bootstrapNumber;
            
                System.out.println(gffDataset.getSequenceType() + "\t" + gffDataset.getSequenceId() + "\t" + gffDataset.getSequenceStart() + "\t" + gffDataset.getSequenceEnd() + "\t" + gffDataset.getSequenceStrand() + "\t" + sequenceCount[0] + "\t" + averageBootstrapSequenceCount + "\t" + enrichmentProbability + "\t\t" + gffDataset.getAttributes());
            }
        }
        
        gffParser.close();
        
        bamFileReader.close();
    }
    
    public static void main(String[] args) throws InterruptedException {
        int bootstrapNumber = 1000;
        int minLength = 0;
        int maxLength = 0;
        
        String lociGFFFileName = null;
        String mappingFileName = null;
        String referenceFastaFileName = null;
        String sequenceFastaFileName = null;
        
        if(args.length == 0) {
            System.out.println("Usage (input of strings without quotation marks (\"):");
            System.out.println("-bootstrapNumber <number>\tnumber of bootstrap iterations (default: 1000)");
            System.out.println("-bamMappingFile <path/filename>\tcoordinate sorted BAM file");
            System.out.println("-lociGff <path/filename>\tthe loci to be analyzed (GFF format)");
            System.out.println("-minLength <number>\tminimal sequence length");
            System.out.println("-maxLength <number>\tmaximal sequence length");
            System.out.println("-referenceFasta <path/filename>\treference sequence set (FastA format)");
            System.out.println("-sequenceFasta <path/filename>\twhole sequence set (FastA format)");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    
                    String argumentValue;
                    if(argumentIndex < args.length) {
                        argumentValue = args[argumentIndex];
                    }
                    else {
                        argumentValue = null;
                    }
                    
                    if(argumentTitle.equals("bootstrapNumber")) {
                        bootstrapNumber = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("bamMappingFile")) {
                        mappingFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("lociGff")) {
                        lociGFFFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("minLength")) {
                        minLength = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("maxLength")) {
                        maxLength = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("referenceFasta")) {
                        referenceFastaFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("sequenceFasta")) {
                        sequenceFastaFileName = argumentValue;
                    }
                }
            }
        }
        
        if((mappingFileName == null) || (lociGFFFileName == null) || (referenceFastaFileName == null) || (sequenceFastaFileName == null)) {
            System.out.println("Usage (input of strings without quotation marks (\"):");
            System.out.println("-bootstrapNumber <number>\tnumber of bootstrap iterations (default: 1000)");
            System.out.println("-bamMappingFile <path/filename>\tcoordinate sorted BAM file");
            System.out.println("-lociGff <path/filename>\tthe loci to be analyzed (GFF format)");
            System.out.println("-minLength <number>\tminimal sequence length");
            System.out.println("-maxLength <number>\tmaximal sequence length");
            System.out.println("-referenceFasta <path/filename>\treference sequence set (FastA format)");
            System.out.println("-sequenceFasta <path/filename>\twhole sequence set (FastA format)");
            System.exit(1);
        }

        try {
            BootstrapMappedSmallRNARegionalEnrichmentAnalysis bootstrapDistributionReferenceLengthAnalysis = new BootstrapMappedSmallRNARegionalEnrichmentAnalysis(referenceFastaFileName, sequenceFastaFileName, minLength, maxLength, bootstrapNumber);
            bootstrapDistributionReferenceLengthAnalysis.calculateBootstrapProbability(mappingFileName, lociGFFFileName);
        } catch (IOException e) {
            System.out.println("I/O-exception: " + e);
        }
    }
    
    private final int bootstrapNumber;
    private final int minLength;
    private final int maxLength;

    private final ReferenceLengthBootstrapDistribution referenceLengthBootstrapDistribution;
    
    private final String sequenceFastaFileName;
}
