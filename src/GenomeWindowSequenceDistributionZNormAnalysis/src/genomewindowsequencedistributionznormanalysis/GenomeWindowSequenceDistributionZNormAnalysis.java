package genomewindowsequencedistributionznormanalysis;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class GenomeWindowSequenceDistributionZNormAnalysis {
    public GenomeWindowSequenceDistributionZNormAnalysis(int readLength, DatabaseLoginData databaseLoginData) throws SQLException {
        this.databaseLoginData = databaseLoginData;
    }
    
    private void getSequenceMappingData(int readLength, String referenceSequenceSetFileName) throws SQLException, FileNotFoundException, IOException {
        HashMap<String,Boolean> referenceSetHashMap = new HashMap();

        if(referenceSequenceSetFileName != null) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(referenceSequenceSetFileName));
        
            String srnaIndexFileLine;
            while((srnaIndexFileLine = bufferedReader.readLine()) != null) {
                if(!srnaIndexFileLine.isEmpty()) {
                    referenceSetHashMap.put(srnaIndexFileLine, Boolean.TRUE);
                }
            }
                
            bufferedReader.close();
        }
        
        sequenceMappingCountHashMap = new HashMap();
        
        DatabaseConnection sequenceDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);

        String sequenceQuery = "SELECT sequence_id FROM srna_mapping_status WHERE mapping_count>0";
        if(readLength > 0) {
            sequenceQuery = "SELECT srna_sequence.sequence_id FROM srna_mapping_status, srna_sequence WHERE srna_sequence.sequence_id=srna_mapping_status.sequence_id AND srna_sequence.length=" + readLength + " AND srna_mapping_status.mapping_count>0";
        }
        
        ResultSet sequenceDataResultSet = sequenceDataStatement.executeQuery(sequenceQuery);
        while(sequenceDataResultSet.next()) {
            String sequenceIndex = sequenceDataResultSet.getString("sequence_id");

            if(referenceSetHashMap.isEmpty() || (referenceSetHashMap.get(sequenceIndex) != null)) {
                sequenceMappingCountHashMap.put(sequenceIndex, Boolean.TRUE);
            }
        }
        sequenceDataResultSet.close();
        sequenceDataStatement.close();
        sequenceDataDatabaseConnection.close();
    }
    
    public void calculateGenomeWindowClusterExpression(String bamMappingFileName, String referenceSetFileName, int readLength, int windowLength) throws IOException, SQLException {
        if(readLength > 0) {
            this.getSequenceMappingData(readLength, referenceSetFileName);
        }
        
        HashMap<String,ArrayList<Integer>> chromosomeDistinctReadCountHashMap = new HashMap();
        
        int distinctReadCount = 0;
        
        System.out.println("chromosome\twindow_start\twindow_end\tdistinct_reads\tdistinct_reads_znorm");

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
            
            String sequenceIndex = bamRecord.getReadName();

            mappingChromosome = bamRecord.getReferenceName();
            if(!mappingChromosome.equals(currentChromosome)) {
                if(mappingChromosome.toLowerCase().contains("ctg")) {
                    continue;
                }

                if(!currentChromosome.equals("undefined")) {
                    chromosomeDistinctReadCountHashMap.get(currentChromosome).add(distinctReadCount);
                }

                distinctReadCount = 0;
                
                currentChromosome = mappingChromosome;
                windowStart = 0;
                
                chromosomeDistinctReadCountHashMap.put(currentChromosome, new ArrayList());
            }
            
            while(bamRecord.getAlignmentStart() >= (windowStart + windowLength)) {
                chromosomeDistinctReadCountHashMap.get(mappingChromosome).add(distinctReadCount);
                
                distinctReadCount = 0;
                
                windowStart += windowLength;
            }
            
            if((readLength <= 0) || (sequenceMappingCountHashMap.get(sequenceIndex) != null)) {
                distinctReadCount++;
            }
        }
        
        if(chromosomeDistinctReadCountHashMap.get(mappingChromosome) != null) {
            chromosomeDistinctReadCountHashMap.get(mappingChromosome).add(distinctReadCount);
        }
        
        double genomeAverageDistinctReadCount = 0;
        double genomeVarianceDistinctReadCount = 0;
        double genomeStandardDeviationDistinctReadCount = 0;
        
        int windowNumber = 0; 
        
        for(String chromosome : chromosomeDistinctReadCountHashMap.keySet()) {
            for(int windowIndex = 0; windowIndex < chromosomeDistinctReadCountHashMap.get(chromosome).size(); windowIndex++) {
                genomeAverageDistinctReadCount += ((double) chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex));

                windowNumber++;
            }
        }
        
        genomeAverageDistinctReadCount /= (double) windowNumber;
            
        for(String chromosome : chromosomeDistinctReadCountHashMap.keySet()) {
            for(int windowIndex = 0; windowIndex < chromosomeDistinctReadCountHashMap.get(chromosome).size(); windowIndex++) {
                genomeVarianceDistinctReadCount += Math.pow((double) (chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) - genomeAverageDistinctReadCount), 2);
            }
        }
        
        genomeVarianceDistinctReadCount /= (double) windowNumber;
        
        for(String chromosome : chromosomeDistinctReadCountHashMap.keySet()) {
            for(int windowIndex = 0; windowIndex < chromosomeDistinctReadCountHashMap.get(chromosome).size(); windowIndex++) {
                genomeStandardDeviationDistinctReadCount = Math.sqrt(genomeVarianceDistinctReadCount);
                
                double referenceDistinctReadCountZNorm = ((double) (chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) - genomeAverageDistinctReadCount) / (double) genomeStandardDeviationDistinctReadCount);

                System.out.println(chromosome + "\t" + (windowIndex * windowLength) + "\t" + (((windowIndex + 1) * windowLength) - 1) + "\t" + chromosomeDistinctReadCountHashMap.get(chromosome).get(windowIndex) + "\t" + referenceDistinctReadCountZNorm);
            }
        }
    }
    
    public static void main(String[] args) {
        int readLength = 0;
        int windowResolution = 1000;
        
        String bamMappingFileName = null;
        String referenceFileName = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-bamMapping <filename>");
            System.out.println("-readLength <length>");
            System.out.println("-reference <filename>\t(optional)");
            System.out.println("-windowLength <width> \t (default: 1000 -> 1 Mbp (1 -> 1000 bp)");
            System.out.println("-databaseUser <username>");
            System.out.println("-bamMapping <password>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];

                    if(argumentTitle.equals("bamMapping")) {
                        bamMappingFileName = argumentValue;
                    }
                    if(argumentTitle.equals("readLength")) {
                        readLength = Integer.valueOf(argumentValue);
                    }
                    if(argumentTitle.equals("reference")) {
                        referenceFileName = argumentValue;
                    } 
                    if(argumentTitle.equals("windowLength")) {
                        windowResolution = Integer.valueOf(argumentValue);
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    } 
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    } 
                }
            }
        }
        
        if((bamMappingFileName == null) || (databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-bamMapping <filename>");
            System.out.println("-readLength <length>");
            System.out.println("-reference <length>\t(optional)");
            System.out.println("-windowLength <width> \t (default: 1000 -> 1 Mbp (1 -> 1000 bp)");
            System.out.println("-databaseUser <username>");
            System.out.println("-bamMapping <password>");
            System.exit(1);
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            GenomeWindowSequenceDistributionZNormAnalysis genomeWindowSequenceDistributionZNormAnalysis = new GenomeWindowSequenceDistributionZNormAnalysis(readLength, databaseLoginData);
            genomeWindowSequenceDistributionZNormAnalysis.calculateGenomeWindowClusterExpression(bamMappingFileName, referenceFileName, readLength, windowResolution);
        } catch (IOException e) {
            System.out.println(e);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    private final DatabaseLoginData databaseLoginData;
    
    private HashMap<String,Boolean> sequenceMappingCountHashMap;
}
