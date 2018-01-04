package extractfastamatchingsequenceexpression;

import de.uni_hamburg.fseifert.fasta.FastaParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ExtractFastaMatchingSequenceExpression {
    public ExtractFastaMatchingSequenceExpression() {}
    
    public void extractSequenceExpressionData(String referenceFastaFileName, String expressionDataFileName) throws IOException {
        ArrayList<String> sequenceArrayList = new ArrayList();
        
        FastaParser fastaParser = new FastaParser(referenceFastaFileName);
        while(fastaParser.nextSequence()) {
            sequenceArrayList.add(fastaParser.nextSequenceFragment());
        }
        fastaParser.close();
        
        String fileLine;
        BufferedReader expressionDataBufferedReader = new BufferedReader(new FileReader(expressionDataFileName));
        while((fileLine = expressionDataBufferedReader.readLine()) != null) {
            String[] fileLineParts = fileLine.split("\t");
            
            if(sequenceArrayList.contains(fileLineParts[0])) {
                System.out.println(fileLine);
            }
        }
        expressionDataBufferedReader.close();
    }
    
    public static void main(String[] args) {
        String referenceFastaFileName = null;
        String expressionDataFileName = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-reference\tFastA file containing the sequences of interest");
            System.out.println("-expression\tfile containing tab-separated expression data, line starting with sequence");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("reference")) {
                        referenceFastaFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("expression")) {
                        expressionDataFileName = argumentValue;                    
                    }    
                }
            }
        }
        
        if((referenceFastaFileName != null) && (expressionDataFileName != null)) {
            ExtractFastaMatchingSequenceExpression extractFastaMatchingSequenceExpression = new ExtractFastaMatchingSequenceExpression();
            try {
                extractFastaMatchingSequenceExpression.extractSequenceExpressionData(referenceFastaFileName, expressionDataFileName);
            }
            catch(IOException e) {
                System.out.println(e);
            }
        }
        else {
            System.out.println("Usage:");
            System.out.println("-reference\tFastA file containing the sequences of interest");
            System.out.println("-expression\tfile containing tab-separated expression data, line starting with sequence");
            System.exit(1);
        }
    }
}
