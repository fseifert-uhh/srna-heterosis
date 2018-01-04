package databaseannotationclustergeneration;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class DatabaseWindowClusterGeneration {
    public DatabaseWindowClusterGeneration(String bamMappingFileName, String databaseUser, String databasePassword) throws SQLException {
        databaseConnection = new DatabaseConnection("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
     
        this.bamMappingFileName = bamMappingFileName;
    }
    
    public final void close() throws SQLException {
        databaseConnection.close();
    }
    
    public void generateAnnotation() throws SQLException, IOException {
        if(databaseConnection.isActive()) {
            Statement annotationStatement = databaseConnection.getConnection().createStatement();
            
            annotationStatement.executeUpdate("CREATE TABLE IF NOT EXISTS genome_window_srna_cluster (chromosome int(10), window_id int(10) NOT NULL, window_start bigint(20) NOT NULL, window_length int(11) NOT NULL, sequence_id_text longtext CHARACTER SET latin1 NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;");
            annotationStatement.executeUpdate("TRUNCATE genome_window_srna_cluster");
        }
    }
    
    public void generateAnnotationCluster() throws SQLException {
        if(databaseConnection.isActive()) {
            Statement annotationStatement = databaseConnection.getConnection().createStatement();

            SAMFileReader bamFileReader = new SAMFileReader(new File(bamMappingFileName));
            SAMRecordIterator bamFileIterator = bamFileReader.iterator();
            SAMRecord bamRecord = null;

            String currentChromosome = null;
            
            int windowStart = 0;
            int windowLength = 1000; 
            HashMap<String,ArrayList<String>> sequenceWindowStrandReadsHashMap = new HashMap();
            sequenceWindowStrandReadsHashMap.put("minus", new ArrayList());
            sequenceWindowStrandReadsHashMap.put("plus", new ArrayList());
            
            int genomeWindowInsertCount = 0;
            StringBuilder genomeWindowSQLInsertStringBuilder = new StringBuilder("INSERT INTO genome_window_srna_cluster (window_id, chromosome, window_start, window_length, sequence_id_text) VALUES ");
            
            while(bamFileIterator.hasNext()) {
                bamRecord = bamFileIterator.next();
                
                if(bamRecord.getReadUnmappedFlag()) {
                    continue;
                }
                
                String mappingChromosome = bamRecord.getReferenceName();
                if((currentChromosome == null) || !mappingChromosome.equals(currentChromosome)) {
                    if(sequenceWindowStrandReadsHashMap.get("minus").size() > 0) {
                        StringBuilder minusStrandReadsStringBuilder = new StringBuilder();
                        boolean firstReadFlag = true;
                        for(String readName : sequenceWindowStrandReadsHashMap.get("minus")) {
                            if(!firstReadFlag) {
                                minusStrandReadsStringBuilder.append(";");
                            }

                            minusStrandReadsStringBuilder.append(readName);

                            firstReadFlag = false;
                        }
                        annotationStatement.execute("INSERT INTO genome_window_srna_cluster (window_id, chromosome, window_start, window_length, sequence_id_text) VALUES (" + (-1 * ((windowStart / windowLength) + 1)) + ", " + currentChromosome + ", " + windowStart + ", 1000, \"" + minusStrandReadsStringBuilder.toString() + "\");");
                    }

                    if(sequenceWindowStrandReadsHashMap.get("plus").size() > 0) {
                        StringBuilder plusStrandReadsStringBuilder = new StringBuilder();
                        boolean firstReadFlag = true;
                        for(String readName : sequenceWindowStrandReadsHashMap.get("plus")) {
                            if(!firstReadFlag) {
                                plusStrandReadsStringBuilder.append(";");
                            }

                            plusStrandReadsStringBuilder.append(readName);

                            firstReadFlag = false;
                        }

                        annotationStatement.execute("INSERT INTO genome_window_srna_cluster (window_id, chromosome, window_start, window_length, sequence_id_text) VALUES (" + ((windowStart / windowLength) + 1) + ", " + currentChromosome + ", " + windowStart + ", 1000, \"" + plusStrandReadsStringBuilder.toString() + "\");");
                    }
                    
                    currentChromosome = mappingChromosome;
                    
                    try {  
                        int chromosomeNumber = Integer.parseInt(currentChromosome);  
                    }  
                    catch(NumberFormatException e) {
                        continue;  
                    }  
                    
                    sequenceWindowStrandReadsHashMap.put("plus", new ArrayList());
                    sequenceWindowStrandReadsHashMap.put("minus", new ArrayList());
                    windowStart = 0;
                }
                
                String annotationStrandDirection;

                if(bamRecord.getAlignmentStart() > (windowStart + windowLength)) {
                    if(sequenceWindowStrandReadsHashMap.get("minus").size() > 0) {
                        StringBuilder minusStrandReadsStringBuilder = new StringBuilder();
                        boolean firstReadFlag = true;
                        for(String readName : sequenceWindowStrandReadsHashMap.get("minus")) {
                            if(!firstReadFlag) {
                                minusStrandReadsStringBuilder.append(";");
                            }

                            minusStrandReadsStringBuilder.append(readName);

                            firstReadFlag = false;
                        }

                        if(minusStrandReadsStringBuilder.length() > 0) {
                            if(genomeWindowInsertCount > 0) {
                                genomeWindowSQLInsertStringBuilder.append(",");
                            }
                            
                            genomeWindowSQLInsertStringBuilder.append(" (" + (-1 * ((windowStart / windowLength) + 1)) + ", " + currentChromosome + ", " + windowStart + ", 1000, \"" + minusStrandReadsStringBuilder.toString() + "\")");
                            genomeWindowInsertCount++;
                        }
                        
                        sequenceWindowStrandReadsHashMap.put("minus", new ArrayList());
                    }

                    if(sequenceWindowStrandReadsHashMap.get("plus").size() > 0) {
                        StringBuilder plusStrandReadsStringBuilder = new StringBuilder();
                        boolean firstReadFlag = true;
                        for(String readName : sequenceWindowStrandReadsHashMap.get("plus")) {
                            if(!firstReadFlag) {
                                plusStrandReadsStringBuilder.append(";");
                            }

                            plusStrandReadsStringBuilder.append(readName);

                            firstReadFlag = false;
                        }

                        if(plusStrandReadsStringBuilder.length() > 0) {
                            if(genomeWindowInsertCount > 0) {
                                genomeWindowSQLInsertStringBuilder.append(",");
                            }
                            
                            genomeWindowSQLInsertStringBuilder.append(" (" + ((windowStart / windowLength) + 1) + ", " + currentChromosome + ", " + windowStart + ", 1000, \"" + plusStrandReadsStringBuilder.toString() + "\")");
                            genomeWindowInsertCount++;
                        }
                        
                        sequenceWindowStrandReadsHashMap.put("plus", new ArrayList());
                    }
                    
                    if(genomeWindowInsertCount >= 100) {
                        annotationStatement.execute(genomeWindowSQLInsertStringBuilder.toString());
                        
                        genomeWindowInsertCount = 0;
                        genomeWindowSQLInsertStringBuilder = new StringBuilder("INSERT INTO genome_window_srna_cluster (window_id, chromosome, window_start, window_length, sequence_id_text) VALUES ");
                    }
                    
                    windowStart += windowLength;
                }
                
                if(bamRecord.getReadNegativeStrandFlag()) {
                    sequenceWindowStrandReadsHashMap.get("minus").add(bamRecord.getReadName());
                }
                else {
                    sequenceWindowStrandReadsHashMap.get("plus").add(bamRecord.getReadName());
                }
            }
            
            if(sequenceWindowStrandReadsHashMap.get("minus").size() > 0) {
                StringBuilder minusStrandReadsStringBuilder = new StringBuilder();
                boolean firstReadFlag = true;
                for(String readName : sequenceWindowStrandReadsHashMap.get("minus")) {
                    if(!firstReadFlag) {
                        minusStrandReadsStringBuilder.append(";");
                    }

                    minusStrandReadsStringBuilder.append(readName);

                    firstReadFlag = false;
                }
                annotationStatement.execute("INSERT INTO genome_window_srna_cluster (window_id, chromosome, window_start, window_length, sequence_id_text) VALUES (" + (-1 * ((windowStart / windowLength) + 1)) + ", " + currentChromosome + ", " + windowStart + ", 1000, \"" + minusStrandReadsStringBuilder.toString() + "\");");
            }

            if(sequenceWindowStrandReadsHashMap.get("plus").size() > 0) {
                StringBuilder plusStrandReadsStringBuilder = new StringBuilder();
                boolean firstReadFlag = true;
                for(String readName : sequenceWindowStrandReadsHashMap.get("plus")) {
                    if(!firstReadFlag) {
                        plusStrandReadsStringBuilder.append(";");
                    }

                    plusStrandReadsStringBuilder.append(readName);

                    firstReadFlag = false;
                }

                annotationStatement.execute("INSERT INTO genome_window_srna_cluster (window_id, chromosome, window_start, window_length, sequence_id_text) VALUES (" + ((windowStart / windowLength) + 1) + ", " + currentChromosome + ", " + windowStart + ", 1000, \"" + plusStrandReadsStringBuilder.toString() + "\");");
            }
        
            bamFileReader.close();
            
            annotationStatement.close();
        }
    }
    
    public static void main(String[] args) {
        String bamMappingFileName = null;
        String databaseUser = null;
        String databasePassword = null;

        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-bamMappingFilename <file name>\tthe name of the BAM mapping file");
            System.out.println("-databaseUser <user>\tthe mysql user name");
            System.out.println("-databasePassword <password>\tthe password for the given mysql user");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("bamMappingFilename")) {
                        bamMappingFileName = argumentValue;
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
            System.out.println("-bamMappingFilename <file name>\tthe name of the BAM mapping file");
            System.out.println("-databaseUser <user>\tthe mysql user name");
            System.out.println("-databasePassword <password>\tthe password for the given mysql user");
            System.exit(1);
        }
        
        try {
            DatabaseWindowClusterGeneration databaseAnnotationClusterGeneration = new DatabaseWindowClusterGeneration(bamMappingFileName, databaseUser, databasePassword);
            databaseAnnotationClusterGeneration.generateAnnotation();
            databaseAnnotationClusterGeneration.generateAnnotationCluster();
            databaseAnnotationClusterGeneration.close();
        } catch(IOException e) {
            System.out.println(e);
        } catch(SQLException e) {
            System.out.println(e);
        }
    }
    
    private final DatabaseConnection databaseConnection;
    
    private final String bamMappingFileName;
}
