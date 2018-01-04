package de.uni_hamburg.fseifert.bootstrap;

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
        
        bootstrapDistributionTreeMap = new TreeMap();
    }

    public ReferenceLengthBootstrapDistribution(String fastaFileName, int minLength, int maxLength) throws IOException {
        this.minLength = minLength;
        this.maxLength = maxLength;        

        this.setSequenceSet(fastaFileName);
        
        bootstrapDistributionTreeMap = new TreeMap();
    }
    
    public void generateBootstrapDistribution(int bootstrapNumber) {
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
    }
    
    public Set<Integer> getBootstrapSequenceIndices() {
        return bootstrapDistributionTreeMap.keySet();
    }
    
    public ArrayList<Integer> getBootstrapSequencePresence(int sequenceIndex) {
        return bootstrapDistributionTreeMap.get(sequenceIndex);
    }
    
    private void setSequenceSet(String fastaFileName) throws IOException {
        int sequenceIndex = 0;
        
        sequenceSetFastaFileName = fastaFileName;
                
        sequenceLengthIndexHashMap = new HashMap();
        
        FastaParser fastaParser = null;
        
        try {
            fastaParser = new FastaParser(fastaFileName);
            while(fastaParser.nextSequence()) {
                int sequenceLength = fastaParser.nextSequenceFragment().length();

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

                sequenceIndex++;
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

                if((minLength > 0) && (sequence.length() < minLength)) {
                    continue;
                }

                if((maxLength > 0) && (sequence.length() > maxLength)) {
                    continue;
                }

                if(referenceSequenceTreeMap.get(sequence) == null) {
                    referenceSequenceTreeMap.put(sequence, 1);
                }
                else {
                    referenceSequenceTreeMap.put(sequence, (referenceSequenceTreeMap.get(sequence) + 1));
                }

                Integer sequenceLengthCount = referenceLengthDistributionHashMap.get(sequence.length());
                if(sequenceLengthCount == null) {
                    referenceLengthDistributionHashMap.put(sequence.length(), referenceSequenceTreeMap.get(sequence));
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
        
        FastaParser sequenceSetFastaParser = null;
                
        try {
            sequenceSetFastaParser = new FastaParser(sequenceSetFastaFileName);
        
            int sequenceIndex = 0;

            for(String referenceSequence : referenceSequenceTreeMap.keySet()) {
                while(sequenceSetFastaParser.nextSequence()) {
                    String sequence = sequenceSetFastaParser.nextSequenceFragment();

                    if(sequence.equals(referenceSequence)) {
                        if(bootstrapDistributionTreeMap.get(sequenceIndex) == null) {
                            bootstrapDistributionTreeMap.put(sequenceIndex, new ArrayList());
                        }

                        bootstrapDistributionTreeMap.get(sequenceIndex).add(0);

                        sequenceIndex++;
                        break;
                    }

                    sequenceIndex++;
                }
            }
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
    
    private TreeMap<Integer,ArrayList<Integer>> bootstrapDistributionTreeMap;
}
