package fastasequencematchextract;

import de.uni_hamburg.fseifert.fasta.FastaParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FastaSequenceMatchExtract {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String referenceFasta = null;
        String sequenceFile = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-referenceFasta <filename>");
            System.out.println("-sequences <filename>");
            System.exit(1);
        }

        for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
            if(args[argumentIndex].startsWith("-")) {
                String argumentTitle = args[argumentIndex].substring(1);
                argumentIndex++;
                String argumentValue = args[argumentIndex];

                if(argumentTitle.equals("referenceFasta")) {
                    referenceFasta = argumentValue;
                }
                if(argumentTitle.equals("sequences")) {
                    sequenceFile = argumentValue;
                }
            }
        }

        if((referenceFasta == null) || (sequenceFile == null)) {
            System.out.println("Usage:");
            System.out.println("-referenceFasta <filename>");
            System.out.println("-sequences <filename>");
            System.exit(1);
        }

        FastaParser referenceFastaParser = new FastaParser(referenceFasta);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(sequenceFile));

        String referenceIndex = null;
        String referenceSequence = null;
        String querySequence = null;
        
        boolean referenceFlag = false;
        boolean subsetQueryFlag = false;
        
        boolean referenceNextFlag = true;
        boolean subsetQueryNextFlag = true;
        
        do {
            if(referenceSequence != null) {
                int comparison = referenceSequence.compareToIgnoreCase(querySequence);
                if(comparison == 0) {
                    System.out.println(">" + referenceIndex);
                    System.out.println(referenceSequence);
                    System.out.println();

                    referenceNextFlag = true;
                    subsetQueryNextFlag = true;
                }
                if(comparison < 0) {
                    referenceNextFlag = true;
                }
                else {
                    subsetQueryNextFlag = true;
                }
            }
            
            if(referenceNextFlag) {
                referenceFlag = referenceFastaParser.nextSequence();
            
                if(referenceFlag) {
                    referenceIndex = referenceFastaParser.getSequenceTitle();
                    referenceSequence = referenceFastaParser.nextSequenceFragment();
                }
            }
            
            if(subsetQueryNextFlag) {
                querySequence = bufferedReader.readLine();
                
                subsetQueryFlag = (querySequence != null);
            }
        } while(referenceFlag && subsetQueryFlag);
    }
}
