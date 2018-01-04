package sequenceexpressionnumberedsequencefastagenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SequenceExpressionNumberedSequenceFastaGenerator {
    public SequenceExpressionNumberedSequenceFastaGenerator() {}
    
    public static void generateFastaData(String sequenceDataFileName, int minLength, int maxLength) throws IOException {
        int sequenceIndex = 0;
        String fileLine;
        
        BufferedReader sequenceDataBufferedReader = new BufferedReader(new FileReader(sequenceDataFileName));
        
        sequenceDataBufferedReader.readLine(); // fetch header line;
        
        while((fileLine = sequenceDataBufferedReader.readLine()) != null) {
            String[] fileLineParts = fileLine.split("\t");
            
            if((fileLineParts[0].length() >= minLength) && (fileLineParts[0].length() <= maxLength)) {
                System.out.println(">" +  sequenceIndex);
                System.out.println(fileLineParts[0]);
                System.out.println();
                
                sequenceIndex++;
            }
        }
        sequenceDataBufferedReader.close();
    }
    
    public static void main(String[] args) {
        String sequenceDataFileName = null;
        int minLength = 15;
        int maxLength = 40;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-sequence\tfile containing sRNA expression data");
            System.out.println("-minLength\tminimal sequence length (default: 15)");
            System.out.println("-maxLength\tminimal sequence length (default: 40)");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("sequence")) {
                        sequenceDataFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("minLength")) {
                        minLength = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("maxLength")) {
                        maxLength = Integer.valueOf(argumentValue);
                    }
                }
            }
        }
        
        if(sequenceDataFileName == null) {
            System.out.println("Usage:");
            System.out.println("-sequence\tfile containing sRNA indices");
            System.out.println("-minLength\tminimal sequence length");
            System.out.println("-maxLength\tminimal sequence length");
            System.exit(1);            
        }
        
        try {
            SequenceExpressionNumberedSequenceFastaGenerator.generateFastaData(sequenceDataFileName, minLength, maxLength);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
