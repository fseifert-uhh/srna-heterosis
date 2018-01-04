package genomewindowassociatedsequencelengthmappingdistributionbootstrapznormanalysis;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class GenomeWindowAssociatedSequenceLengthMappingDistributionBootstrapZNormAnalysis {
    public GenomeWindowAssociatedSequenceLengthMappingDistributionBootstrapZNormAnalysis(int readLength, DatabaseLoginData databaseLoginData) throws SQLException {
        this.databaseLoginData = databaseLoginData;

        getSequenceMappingData(readLength);
    }
    
    public void calculateGenomeWindowClusterExpression(String sequenceSubsetFileName, String bamMappingFileName, int readLength, int windowLength, int bootstrapNumber, double foldEnrichment) throws IOException, SQLException {
        HashMap<Integer,HashMap<Integer,Boolean>> sequenceBootstrapSubsetHashMap = sequenceBootstrapSubsetGeneration(sequenceSubsetFileName, bootstrapNumber);
        
        HashMap<String,ArrayList<Integer>> chromosomeDistinctReadCountHashMap = new HashMap();
        HashMap<String,ArrayList<Integer[]>> bootstrapChromosomeDistinctReadCountHashMap = new HashMap();
        
        int distinctReadCount = 0;
        Integer[] bootstrapDistinctReadCount = new Integer[bootstrapNumber];
        for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
            bootstrapDistinctReadCount[bootstrapIndex] = 0;
        }
        
        double bootstrapDepletionProbability;
        double bootstrapEnrichmentProbability;
        
        System.out.println("chromosome\twindow_start\twindow_end\tdistinct_reads\taverage_bootstrap_distinct_reads\td-istinct_reads_znorm\taverage_bootstrap_distinct_reads_znorm\tdepletion_probability\tenrichment_probability");

        SAMFileReader bamFileReader = new SAMFileReader(new File(bamMappingFileName));
        SAMRecordIterator bamFileIterator = bamFileReader.iterator();
        SAMRecord bamRecord;
        
        String currentChromosome = "undefined";
        String mappingChromosome = null;
        long windowStart = 0;
        
        while(bamFileIterator.hasNext()) {
            bamRecord = bamFileIterator.next();

            if(bamRecord.getReadUnmappedFlag()) {
                continue;
            }
            
            int sequenceIndex = Integer.valueOf(bamRecord.getReadName());
            if(sequenceBootstrapSubsetHashMap.get(sequenceIndex) == null) {
                continue;               
            }

            mappingChromosome = bamRecord.getReferenceName();
            if(!mappingChromosome.equals(currentChromosome)) {
                if(mappingChromosome.toLowerCase().contains("ctg")) {
                    continue;
                }

                if(!currentChromosome.equals("undefined")) {
                    chromosomeDistinctReadCountHashMap.get(currentChromosome).add(distinctReadCount);
                    bootstrapChromosomeDistinctReadCountHashMap.get(currentChromosome).add(bootstrapDistinctReadCount);
                }

                distinctReadCount = 0;
                bootstrapDistinctReadCount = new Integer[bootstrapNumber];
                for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
                    bootstrapDistinctReadCount[bootstrapIndex] = 0;
                } 
                
                currentChromosome = mappingChromosome;
                windowStart = 0;
                
                chromosomeDistinctReadCountHashMap.put(currentChromosome, new ArrayList());
                bootstrapChromosomeDistinctReadCountHashMap.put(currentChromosome, new ArrayList());
            }
            
            while(bamRecord.getAlignmentStart() >= (windowStart + windowLength)) {
                chromosomeDistinctReadCountHashMap.get(mappingChromosome).add(distinctReadCount);
                bootstrapChromosomeDistinctReadCountHashMap.get(mappingChromosome).add(bootstrapDistinctReadCount);
                
                distinctReadCount = 0;
                bootstrapDistinctReadCount = new Integer[bootstrapNumber];
                for(int bootstrapIndex = 1; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                   bootstrapDistinctReadCount[bootstrapIndex - 1] = 0;
                }
                
                windowStart += windowLength;
            }
            
            for(int bootstrapIndex : sequenceBootstrapSubsetHashMap.get(sequenceIndex).keySet()) {
                if(bootstrapIndex == 0) {
                    distinctReadCount++;
                }
                else {
                    bootstrapDistinctReadCount[bootstrapIndex - 1]++;
                }
            }
        }
        
        if(chromosomeDistinctReadCountHashMap.get(mappingChromosome) != null) {
            chromosomeDistinctReadCountHashMap.get(mappingChromosome).add(distinctReadCount);
            bootstrapChromosomeDistinctReadCountHashMap.get(mappingChromosome).add(bootstrapDistinctReadCount);
        }
        
        double genomeAverageDistinctReadCount = 0;
        double genomeVarianceDistinctReadCount = 0;
        double genomeStandardDeviationDistinctReadCount = 0;
        double[] bootstrapGenomeAverageDistinctReadCount = new double[bootstrapNumber];
        double[] bootstrapGenomeVarianceDistinctReadCount = new double[bootstrapNumber];
        double[] bootstrapGenomeStandardDeviationDistinctReadCount = new double[bootstrapNumber];
        
        
        int windowNumber = 0; 
        
        for(String chromosome : chromosomeDistinctReadCountHashMap.keySet()) {
            for(int windowIndex = 0; windowIndex < chromosomeDistinctReadCountHashMap.get(chromosome).size(); windowIndex++) {
                genomeAverageDistinctReadCount += ((double) chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex));

                for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
                    bootstrapGenomeAverageDistinctReadCount[bootstrapIndex] += ((double) bootstrapChromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex)[bootstrapIndex]);
                }
                
                windowNumber++;
            }
        }
        
        genomeAverageDistinctReadCount /= (double) windowNumber;
            
        for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
            bootstrapGenomeAverageDistinctReadCount[bootstrapIndex] /= (double) windowNumber;
        }
                
        for(String chromosome : chromosomeDistinctReadCountHashMap.keySet()) {
            for(int windowIndex = 0; windowIndex < chromosomeDistinctReadCountHashMap.get(chromosome).size(); windowIndex++) {
                genomeVarianceDistinctReadCount += Math.pow((double) (chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) - genomeAverageDistinctReadCount), 2);

                for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
                    bootstrapGenomeVarianceDistinctReadCount[bootstrapIndex] += Math.pow((double) (bootstrapChromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex)[bootstrapIndex] - bootstrapGenomeAverageDistinctReadCount[bootstrapIndex]), 2);
                }
            }
        }
        
        genomeVarianceDistinctReadCount /= (double) windowNumber;
        
        for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
            bootstrapGenomeVarianceDistinctReadCount[bootstrapIndex] /= (double) windowNumber;
        }
        
        for(String chromosome : chromosomeDistinctReadCountHashMap.keySet()) {
            for(int windowIndex = 0; windowIndex < chromosomeDistinctReadCountHashMap.get(chromosome).size(); windowIndex++) {
                genomeStandardDeviationDistinctReadCount = Math.sqrt(genomeVarianceDistinctReadCount);
                
                for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
                    bootstrapGenomeStandardDeviationDistinctReadCount[bootstrapIndex] = Math.sqrt(bootstrapGenomeVarianceDistinctReadCount[bootstrapIndex]);
                }
            
                double referenceDistinctReadCountZNorm = ((double) (chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) - genomeAverageDistinctReadCount) / (double) genomeStandardDeviationDistinctReadCount);
                double averageBootstrapDistinctReadCount = 0;
                double averageBootstrapDistinctReadCountZNorm = 0;

                bootstrapDepletionProbability = 0;
                bootstrapEnrichmentProbability = 0;
                for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
                    averageBootstrapDistinctReadCount += bootstrapChromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex)[bootstrapIndex];

                    double bootstrapDistinctReadCountZNorm = ((double) (bootstrapChromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex)[bootstrapIndex] - bootstrapGenomeAverageDistinctReadCount[bootstrapIndex]) / (double) bootstrapGenomeStandardDeviationDistinctReadCount[bootstrapIndex]);
                    averageBootstrapDistinctReadCountZNorm += bootstrapDistinctReadCountZNorm;

                    if(chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) <= (foldEnrichment * ((double) bootstrapChromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex)[bootstrapIndex]))) {
                        bootstrapEnrichmentProbability += (1.0 / (double) bootstrapNumber);
                    }
                    else if(chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) >= (foldEnrichment * ((double) bootstrapChromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex)[bootstrapIndex]))) {
                        bootstrapDepletionProbability += (1.0 / (double) bootstrapNumber);
                    }
                }
                
                averageBootstrapDistinctReadCount /= (double) bootstrapNumber;
                averageBootstrapDistinctReadCountZNorm /= (double) bootstrapNumber;

                System.out.println(chromosome + "\t" + (windowIndex * windowLength) + "\t" + (((windowIndex + 1) * windowLength) - 1) + "\t" + chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) + "\t" + averageBootstrapDistinctReadCount + "\t" + referenceDistinctReadCountZNorm + "\t" + averageBootstrapDistinctReadCountZNorm + "\t" + bootstrapDepletionProbability + "\t" + bootstrapEnrichmentProbability);
            }
        }
    }
    
    private void getSequenceMappingData(int readLength) throws SQLException {
        sequenceMappingCountHashMap = new HashMap();
        
        DatabaseConnection sequenceDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);

        String sequenceQuery = null;
        if(readLength > 0) {
            sequenceQuery = "SELECT * FROM srna_mapping_status_refgen4, srna_sequence WHERE srna_sequence.sequence_id=srna_mapping_status_refgen4.sequence_id AND srna_sequence.length=" + readLength + " AND srna_mapping_status_refgen4.mapping_count>0";
        }
        else {
            System.out.println("bootstrap not working for multiple lengths, specify a length");
        }
        
        ResultSet sequenceDataResultSet = sequenceDataStatement.executeQuery(sequenceQuery);
        while(sequenceDataResultSet.next()) {
            int sequenceIndex = sequenceDataResultSet.getInt("sequence_id");
            int mappingCount = sequenceDataResultSet.getInt("mapping_count");

            sequenceMappingCountHashMap.put(sequenceIndex, mappingCount);
        }
        sequenceDataResultSet.close();
        sequenceDataStatement.close();
        sequenceDataDatabaseConnection.close();
    }
    
    public HashMap<Integer,HashMap<Integer,Boolean>> sequenceBootstrapSubsetGeneration(String referenceSequenceSetFileName, int bootstrapNumber) throws IOException, SQLException {
        int[] mappingSequenceIndices = new int[sequenceMappingCountHashMap.size()];
        int arrayIndex = 0;
        for(int sequenceIndex : sequenceMappingCountHashMap.keySet()) {
            mappingSequenceIndices[arrayIndex++] = sequenceIndex;
        }
        
        HashMap<Integer,HashMap<Integer,Boolean>> bootstrapSequenceSetHashMap = new HashMap();        
        
        BufferedReader bufferedReader = new BufferedReader(new FileReader(referenceSequenceSetFileName));
        
        String srnaIndexFileLine;
        while((srnaIndexFileLine = bufferedReader.readLine()) != null) {
            if(!srnaIndexFileLine.isEmpty()) {
                int sequenceIndex = Integer.valueOf(srnaIndexFileLine);
                
                if(sequenceMappingCountHashMap.get(sequenceIndex) != null) {
                    HashMap<Integer,Boolean> bootstrapIndexHashMap = new HashMap();
                    bootstrapIndexHashMap.put(0, true);
                    
                    bootstrapSequenceSetHashMap.put(sequenceIndex, bootstrapIndexHashMap);
                }
            }
        }
        
        int mappingReferenceSequenceCount = bootstrapSequenceSetHashMap.size();
                
        bufferedReader.close();
        
        Random random = new Random();
        
        for(int bootstrapIndex = 1; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
            int bootstrapSequenceSubsetSize = 0;

            do {
                int randomIndex = random.nextInt(mappingSequenceIndices.length);

                HashMap<Integer,Boolean> bootstrapIndexHashMap;
                if(bootstrapSequenceSetHashMap.get(mappingSequenceIndices[randomIndex]) == null) {
                    bootstrapIndexHashMap = new HashMap();
                }
                else {
                    bootstrapIndexHashMap = bootstrapSequenceSetHashMap.get(mappingSequenceIndices[randomIndex]);

                    if(bootstrapIndexHashMap.get(bootstrapIndex) != null) {
                        continue;
                    }
                }

                bootstrapIndexHashMap.put(bootstrapIndex, true);
                bootstrapSequenceSetHashMap.put(mappingSequenceIndices[randomIndex], bootstrapIndexHashMap);

                bootstrapSequenceSubsetSize++;
            } while(bootstrapSequenceSubsetSize < mappingReferenceSequenceCount);
        }
        
        return bootstrapSequenceSetHashMap;
    }
    
    public static void main(String[] args) {
        double foldEnrichment = 1;
        
        int bootstrapNumber = 1000;
        int readLength = 0;
        int windowLength = 1000000;

        String bamMappingFileName = null;
        String sequenceSubsetIndicesFileName = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("-bamMappingFile <path/filename> \tsorted BAM alignment file");
            System.out.println("-bootstrap <number of runs>");
            System.out.println("-bamMappingFile <path/filename> \tsorted BAM alignment file");
            System.out.println("-foldEnrichment <factor>");
            System.out.println("-srnaIndicesFile <path/filename> \tfile containing sRNA indices (one per row)");
            System.out.println("-windowLength <bp> \t (default 1000000)");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("bamMappingFile")) {
                        bamMappingFileName = argumentValue;
                    }
                    if(argumentTitle.equals("bootstrap")) {
                        bootstrapNumber = Integer.valueOf(argumentValue);
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                    if(argumentTitle.equals("foldEnrichment")) {
                        foldEnrichment = Double.valueOf(argumentValue);
                    }
                    if(argumentTitle.equals("readLength")) {
                        readLength = Integer.valueOf(argumentValue);
                    }
                    if(argumentTitle.equals("srnaIndicesFile")) {
                        sequenceSubsetIndicesFileName = argumentValue;
                    }
                    if(argumentTitle.equals("windowLength")) {
                        windowLength = Integer.valueOf(argumentValue);
                    }
                }
            }
            
            if((sequenceSubsetIndicesFileName == null) || (bamMappingFileName == null) || (databaseUser == null) || (databasePassword == null)) {
                System.out.println("-bamMappingFile <path/filename> \tsorted BAM alignment file");
                System.out.println("-bootstrap <number of runs>");
                System.out.println("-bamMappingFile <path/filename> \tsorted BAM alignment file");
                System.out.println("-foldEnrichment <factor>");
                System.out.println("-srnaIndicesFile <path/filename> \tfile containing sRNA indices (one per row)");
                System.out.println("-windowLength <bp> \t (default 1000000)");
                System.out.println("-databaseUser <username>");
                System.out.println("-databasePassword <password>");
                System.exit(1);
            }
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            GenomeWindowAssociatedSequenceLengthMappingDistributionBootstrapZNormAnalysis databaseGenomeWindowClusterGeneration = new GenomeWindowAssociatedSequenceLengthMappingDistributionBootstrapZNormAnalysis(readLength, databaseLoginData);
            databaseGenomeWindowClusterGeneration.calculateGenomeWindowClusterExpression(sequenceSubsetIndicesFileName, bamMappingFileName, readLength, windowLength, bootstrapNumber, foldEnrichment);
        } catch (IOException e) {
            System.out.println(e);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    private final DatabaseLoginData databaseLoginData;
    
    private HashMap<Integer,Integer> sequenceMappingCountHashMap;
}
