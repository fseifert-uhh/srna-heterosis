package generegiongffextractionconverter;

import de.uni_hamburg.fseifert.gff.GFFDataset;
import de.uni_hamburg.fseifert.gff.GFFParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class GeneRegionGFFExtractionConverter {
    public GeneRegionGFFExtractionConverter() {}
    
    public static void generateGFFData(String geneIdentifierFileName, String referenceGFFFileName, int upstreamWindowLength, int downstreamWindowLength) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(geneIdentifierFileName));
        
        String fileLine;
        
        HashMap<String,Boolean> geneIdentifierHashMap = new HashMap();
        while((fileLine = bufferedReader.readLine()) != null) {
            geneIdentifierHashMap.put(fileLine, Boolean.TRUE);
        }

        GFFParser gffParser = new GFFParser(referenceGFFFileName);
        while(gffParser.hasNext()) {
            GFFDataset gffDataset = gffParser.next();

            String geneIdentifier;
            try {
                geneIdentifier = gffDataset.getAttributes().split("ID=gene:")[1].split(";")[0];
            } catch(Exception e) {
                continue;
            }
            
            if(geneIdentifierHashMap.get(geneIdentifier) != null) {
                int startPosition = (int) gffDataset.getSequenceStart();
                int endPosition = (int) gffDataset.getSequenceEnd();
            
                System.out.println(gffDataset.getSequenceId() + "\tgene-region\tgene-region\t" + (startPosition - 1000) + "\t" + (endPosition + 1000)  + "\t.\t" + gffDataset.getSequenceStrand() + "\t.\t" + gffDataset.getAttributes());
            }
        }

        gffParser.close();
        
        bufferedReader.close();
    }
    
    public static void main(String[] args) {
        int upstreamWindowExtension = 0;
        int downstreamWindowExtension = 0;
        
        String geneIdentifierFileName = null;
        String referenceGFFFileName = null;
        
        if(args.length == 0) {
            System.out.println("Usage (input of strings without quotation marks (\"):");
            System.out.println("-genes <path/filename>\tgene identifier file");
            System.out.println("-referenceGFF <path/filename>\treference annotation GFF file");
            System.out.println("-upstreamWindow <number>\tupstream region (bp)");
            System.out.println("-downstreamWindow <number>\tdownstream region (bp)");
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
                    
                    if(argumentTitle.equals("genes")) {
                        geneIdentifierFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("downstreamWindow")) {
                        downstreamWindowExtension = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("referenceGFF")) {
                        referenceGFFFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("upstreamWindow")) {
                        upstreamWindowExtension = Integer.valueOf(argumentValue);
                    }
                }
            }
        }
        
        if((geneIdentifierFileName == null) && (referenceGFFFileName == null)) {
            System.out.println("Usage (input of strings without quotation marks (\"):");
            System.out.println("-genes <path/filename>\tgene identifier file");
            System.out.println("-referenceGFF <path/filename>\treference annotation GFF file");
            System.out.println("-upstreamWindow <number>\tupstream region (bp)");
            System.out.println("-downstreamWindow <number>\tdownstream region (bp)");
            System.exit(1);
        }
        
        try {
            GeneRegionGFFExtractionConverter.generateGFFData(geneIdentifierFileName, referenceGFFFileName, upstreamWindowExtension, downstreamWindowExtension);
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }
}
