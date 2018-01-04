package bootstrapsmallrnaregionalenrichmentanalysis;

import de.uni_hamburg.fseifert.fasta.FastaParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class ReferenceLengthBootstrapDistribution {
    public ReferenceLengthBootstrapDistribution(String fastaFileName) throws IOException {
        this.setSequenceSet(fastaFileName);
        
        sequenceHashMap = new HashMap();
        sequenceIndexHashMap = new HashMap();
        
        bootstrapDistributionTreeMap = new TreeMap();
    }

    public ReferenceLengthBootstrapDistribution(String fastaFileName, int minLength, int maxLength) throws IOException {
        this.minLength = minLength;
        this.maxLength = maxLength;        

        this.setSequenceSet(fastaFileName);
        
        sequenceHashMap = new HashMap();
        sequenceIndexHashMap = new HashMap();
        
        bootstrapDistributionTreeMap = new TreeMap();
    }
    
    public void generateBootstrapDistribution(int bootstrapNumber) throws IOException {
        Random random = new Random();
        
        for(int bootstrapIndex = 1; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
            for(int currentBootstrapLength : referenceLengthDistributionHashMap.keySet()) {
                for(int bootstrapLengthIndex = 0; bootstrapLengthIndex < referenceLengthDistributionHashMap.get(currentBootstrapLength); bootstrapLengthIndex++) {
                    int randomIndex = random.nextInt(sequenceLengthIndexHashMap.get(currentBootstrapLength).size());
                    int sequenceIndex = sequenceLengthIndexHashMap.get(currentBootstrapLength).get(randomIndex);

                    if(bootstrapDistributionTreeMap.get(sequenceIndex) == null) {
                        bootstrapDistributionTreeMap.put(sequenceIndex, new ArrayList());
                    }
                
                    bootstrapDistributionTreeMap.get(sequenceIndex).add(bootstrapIndex);
                }
            }
        }
        
        FastaParser fastaParser = null;
        int sequenceIndex = -1;
        
        try {
            fastaParser = new FastaParser(sequenceSetFastaFileName);
            while(fastaParser.nextSequence()) {
                sequenceIndex++;

                if(bootstrapDistributionTreeMap.get(sequenceIndex) != null) {
                    String sequence = fastaParser.nextSequenceFragment();
                
                    sequenceIndexHashMap.put(sequence, sequenceIndex);
                    sequenceHashMap.put(sequenceIndex, sequence);
                }
            }
        } finally {
            if(fastaParser != null) {
                fastaParser.close();
            }
        }
    }
    
    public Set<Integer> getBootstrapSequenceIndices() {
        return bootstrapDistributionTreeMap.keySet();
    }
    
    public ArrayList<Integer> getBootstrapSequencePresence(int sequenceIndex) {
        return bootstrapDistributionTreeMap.get(sequenceIndex);
    }

    public ArrayList<Integer> getBootstrapSequencePresence(String sequence) {
        Integer sequenceIndex = sequenceIndexHashMap.get(sequence);
        
        if(sequenceIndex == null) {
            return null;
        }
        
        return bootstrapDistributionTreeMap.get(sequenceIndex);
    }
    
    private void setSequenceSet(String fastaFileName) throws IOException {
        int sequenceIndex = -1;
        
        sequenceSetFastaFileName = fastaFileName;
                
        sequenceLengthIndexHashMap = new HashMap();
        
        FastaParser fastaParser = null;
        
        try {
            fastaParser = new FastaParser(fastaFileName);
            while(fastaParser.nextSequence()) {
                sequenceIndex++;

                String sequence = fastaParser.nextSequenceFragment();
                int sequenceLength = sequence.length();

                if((minLength > 0) && (sequenceLength < minLength)) {
                    continue;
                }

                if((maxLength > 0) && (sequenceLength > maxLength)) {
                    continue;
                }

                if(sequenceLengthIndexHashMap.get(sequenceLength) == null) {
                    sequenceLengthIndexHashMap.put(sequenceLength, new ArrayList());
                }

                sequenceLengthIndexHashMap.get(sequenceLength).add(sequenceIndex);
            }
        } finally {
            if(fastaParser != null) {
                fastaParser.close();
            }
        }
    }
    
    public void setReferenceDistribution(String referenceFastaFileName) throws IOException {
        referenceLengthDistributionHashMap = new HashMap();
        
        TreeMap<String, Integer> referenceSequenceTreeMap = new TreeMap();
        
        FastaParser fastaParser = null;
                
        try {
            fastaParser = new FastaParser(referenceFastaFileName);

            while(fastaParser.nextSequence()) {
                String sequence = fastaParser.nextSequenceFragment();

                if(minLength > 0) { 
                    if(sequence.length() < minLength) {
                        continue;
                    }
                    
                    if(sequence.length() > maxLength) {
                        continue;
                    }
                }

                if(referenceSequenceTreeMap.get(sequence) == null) {
                    referenceSequenceTreeMap.put(sequence, 1);
                }

                Integer sequenceLengthCount = referenceLengthDistributionHashMap.get(sequence.length());
                if(sequenceLengthCount == null) {
                    referenceLengthDistributionHashMap.put(sequence.length(), 1);
                }
                else {
                    referenceLengthDistributionHashMap.put(sequence.length(), sequenceLengthCount + 1);
                }
            }
        } finally {
            if(fastaParser != null) {
                fastaParser.close();
            }
        }
        
        for(int sequenceLength : referenceLengthDistributionHashMap.keySet()) {
            System.out.println(sequenceLength + " nt: " + referenceLengthDistributionHashMap.get(sequenceLength));
        }
        
        FastaParser sequenceSetFastaParser = null;
                
        try {
            sequenceSetFastaParser = new FastaParser(sequenceSetFastaFileName);
        
            while(sequenceSetFastaParser.nextSequence()) {
                int sequenceIndex = Integer.valueOf(sequenceSetFastaParser.getSequenceTitle());
                String sequence = sequenceSetFastaParser.nextSequenceFragment();
                   
                if(referenceSequenceTreeMap.get(sequence) != null) {
                    bootstrapDistributionTreeMap.put(sequenceIndex, new ArrayList());
                    bootstrapDistributionTreeMap.get(sequenceIndex).add(0);
                }
            }
            
            System.out.println("reference: " + bootstrapDistributionTreeMap.size());
        } finally {
            if(sequenceSetFastaParser != null) {
                sequenceSetFastaParser.close();
            }
        }
    }

    private int minLength = 0;
    private int maxLength = 0;
    
    private HashMap<Integer,Integer> referenceLengthDistributionHashMap;
    private HashMap<Integer,ArrayList<Integer>> sequenceLengthIndexHashMap;
    
    private String sequenceSetFastaFileName;
    
    private HashMap<Integer,String> sequenceHashMap;
    private HashMap<String,Integer> sequenceIndexHashMap;
    
    private TreeMap<Integer,ArrayList<Integer>> bootstrapDistributionTreeMap;
}
