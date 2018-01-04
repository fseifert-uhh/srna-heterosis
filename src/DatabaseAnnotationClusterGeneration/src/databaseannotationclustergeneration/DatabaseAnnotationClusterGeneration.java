package databaseannotationclustergeneration;

import de.uni_hamburg.fseifert.gff.GFFDataset;
import de.uni_hamburg.fseifert.gff.GFFParser;
import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class DatabaseAnnotationClusterGeneration {
    public DatabaseAnnotationClusterGeneration(String bamMappingFileName, String gffAnnotationFileName, String databaseUser, String databasePassword) throws SQLException {
        databaseConnection = new DatabaseConnection("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
     
        this.bamMappingFileName = bamMappingFileName;
        this.gffAnnotationFileName = gffAnnotationFileName;
    }
    
    public final void close() throws SQLException {
        databaseConnection.close();
    }
    
    public void generateAnnotation() throws SQLException, IOException {
        int annotationIndex = 1;
        int annotationPackageCount = 0;
        int negativeStrandLastPosition = 1;
        int positiveStrandLastPosition = 1;
        
        if(databaseConnection.isActive()) {
            Statement annotationStatement = databaseConnection.getConnection().createStatement();
            
            annotationStatement.executeUpdate("CREATE TABLE IF NOT EXISTS genome_annotation (annotation_id int(10) unsigned NOT NULL, chromosome tinyint(3) unsigned NOT NULL, sequence_type varchar(10) COLLATE latin1_general_ci NOT NULL, start_position int(10) unsigned NOT NULL, end_position int(1) unsigned NOT NULL, strand varchar(1) COLLATE latin1_general_ci NOT NULL, annotation_text text COLLATE latin1_general_ci NOT NULL, UNIQUE KEY (annotation_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;");
            annotationStatement.executeUpdate("TRUNCATE genome_annotation");
            annotationStatement.executeUpdate("CREATE TABLE IF NOT EXISTS genome_annotation_srna_cluster (annotation_id int(10) unsigned NOT NULL, strand int(1) unsigned not NULL, antisense int(1) unsigned NOT NULL, sequence_id_text longtext CHARACTER SET latin1 NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;");
            annotationStatement.executeUpdate("TRUNCATE genome_annotation_srna_cluster");
            
            StringBuilder annotationSQLQueryStringBuilder = new StringBuilder("INSERT INTO genome_annotation (annotation_id, chromosome, sequence_type, start_position, end_position, strand, annotation_text) VALUES ");
            
            GFFParser gffParser = new GFFParser(gffAnnotationFileName);
            while(gffParser.hasNext()) {
                GFFDataset gffDataset = gffParser.next();
                
                if(!gffDataset.getSequenceId().matches("^\\d+$")) {
                    continue;
                }
                
                /* insert intergenic annotation */
                if(gffDataset.getSequenceStrand() == '*') {
                    if(positiveStrandLastPosition < (gffDataset.getSequenceStart() - 1)) {
                        if(annotationPackageCount > 0) {
                            annotationSQLQueryStringBuilder.append(",");
                        }

                        annotationSQLQueryStringBuilder.append("(" + annotationIndex + "," + gffDataset.getSequenceId() + ",\"intergenic\"," + positiveStrandLastPosition + "," + (gffDataset.getSequenceStart() - 1) + ",\"+\",\"\")");
                    
                        positiveStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                        
                        annotationIndex++;  
                        annotationPackageCount++;
                    }
                    
                    if(negativeStrandLastPosition < (gffDataset.getSequenceStart() - 1)) {
                        if(annotationPackageCount > 0) {
                            annotationSQLQueryStringBuilder.append(",");
                        }

                        annotationSQLQueryStringBuilder.append("(" + annotationIndex + "," + gffDataset.getSequenceId() + ",\"intergenic\"," + negativeStrandLastPosition + "," + (gffDataset.getSequenceStart() - 1) + ",\"-\",\"\")");
                    
                        negativeStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                        
                        annotationIndex++;  
                        annotationPackageCount++;
                    }
                }        
                if(gffDataset.getSequenceStrand() == '+') {
                    if(positiveStrandLastPosition < (gffDataset.getSequenceStart() - 1)) {
                        if(annotationPackageCount > 0) {
                            annotationSQLQueryStringBuilder.append(",");
                        }

                        annotationSQLQueryStringBuilder.append("(" + annotationIndex + "," + gffDataset.getSequenceId() + ",\"intergenic\"," + positiveStrandLastPosition + "," + (gffDataset.getSequenceStart() - 1) + ",\"+\",\"\")");
                    
                        positiveStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                        
                        annotationIndex++;  
                        annotationPackageCount++;
                    }
                }
                if(gffDataset.getSequenceStrand() == '-') {
                    if(negativeStrandLastPosition < (gffDataset.getSequenceStart() - 1)) {
                        if(annotationPackageCount > 0) {
                            annotationSQLQueryStringBuilder.append(",");
                        }

                        annotationSQLQueryStringBuilder.append("(" + annotationIndex + "," + gffDataset.getSequenceId() + ",\"intergenic\"," + negativeStrandLastPosition + "," + (gffDataset.getSequenceStart() - 1) + ",\"-\",\"\")");
                    
                        negativeStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                        
                        annotationIndex++;  
                        annotationPackageCount++;
                    }
                }
                    
                /* insert gene/repeat annotation */
                if(annotationPackageCount > 0) {
                    annotationSQLQueryStringBuilder.append(",");
                }

                String annotationType = "";
                if(gffDataset.getSequenceType().equals("gene")) {
                    annotationType = "gene";
                }                    
                else {
                    annotationType = "repeat";
                }

                annotationSQLQueryStringBuilder.append("(" + annotationIndex + "," + gffDataset.getSequenceId() + ",\"" + annotationType + "\"," + gffDataset.getSequenceStart() + "," + gffDataset.getSequenceEnd() + ",\"" + gffDataset.getSequenceStrand() +"\",\"" + gffDataset.getAttributes()+ "\")");
                
                if(gffDataset.getSequenceStrand() == '*') {
                    positiveStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                    negativeStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                }
                else if(gffDataset.getSequenceStrand() == '+') {
                    positiveStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                }
                else if(gffDataset.getSequenceStrand() == '-') {
                    negativeStrandLastPosition = (int) (gffDataset.getSequenceEnd() + 1);
                }
                
                annotationIndex++;  
                annotationPackageCount++;
                
                if(annotationPackageCount >= 10000) {
                    annotationStatement.execute(annotationSQLQueryStringBuilder.toString());
                    
                    annotationSQLQueryStringBuilder = new StringBuilder("INSERT INTO genome_annotation (annotation_id, chromosome, sequence_type, start_position, end_position, strand, annotation_text) VALUES ");
                    
                    annotationPackageCount = 0;
                }
            }
            gffParser.close();
            
            annotationStatement.close();
        }
    }
    
    public void generateAnnotationCluster() throws SQLException {
        if(databaseConnection.isActive()) {
            Statement annotationStatement = databaseConnection.getConnection().createStatement();

            SAMFileReader bamFileReader = new SAMFileReader(new File(bamMappingFileName));
            SAMRecordIterator bamFileIterator = bamFileReader.iterator();
            SAMRecord bamRecord = null;

            String currentChromosome = null;
            HashMap<Integer,SequenceAnnotation> sequenceAnnotationHashMap = new HashMap();
            HashMap<Integer,HashMap<String,ArrayList<String>>> sequenceAnnotationReadsHashMap = new HashMap();
            
            int minAnnotationIndex = -1;
            int finishedAnnotationIndex = 0;
            
            String[] strand = {"minus", "plus"};
            String[] antisense = {"sense", "antisense"};
            
            int genomeAnnotationInsertCount = 0;
            StringBuilder genomeAnnotationSQLInsertStringBuilder = new StringBuilder("INSERT INTO genome_annotation_srna_cluster (annotation_id, antisense, strand, sequence_id_text) VALUES ");
            
            while(bamFileIterator.hasNext()) {
                bamRecord = bamFileIterator.next();
                
                if(bamRecord.getReadUnmappedFlag()) {
                    continue;
                }
                
                String mappingChromosome = bamRecord.getReferenceName();
                if((currentChromosome == null) || !mappingChromosome.equals(currentChromosome)) {
                    /* process data from previous chromosome */
                    if(currentChromosome != null) {
                        for(int annotationIndex : sequenceAnnotationReadsHashMap.keySet()) {
                            for(int strandIndex = 0; strandIndex <=1; strandIndex++) {
                                for(int antisenseIndex = 0; antisenseIndex <= 1; antisenseIndex++) {
                                    if(sequenceAnnotationReadsHashMap.get(annotationIndex).get(strand[strandIndex] + "_" + antisense[antisenseIndex]).size() > 0) {
                                        StringBuilder readsStringBuilder = new StringBuilder();
                                        boolean firstReadFlag = true;
                                        for(String readName : sequenceAnnotationReadsHashMap.get(annotationIndex).get(strand[strandIndex] + "_" + antisense[antisenseIndex])) {
                                            if(!firstReadFlag) {
                                                readsStringBuilder.append(";");
                                            }

                                            readsStringBuilder.append(readName);

                                            firstReadFlag = false;
                                        }

                                        if(genomeAnnotationInsertCount > 0) {
                                            genomeAnnotationSQLInsertStringBuilder.append(", ");
                                        }

                                        genomeAnnotationSQLInsertStringBuilder.append(" (" + annotationIndex + ", " + antisenseIndex + ", " + strandIndex + ", \";" + readsStringBuilder.toString() + ";\")");
                                        genomeAnnotationInsertCount++;
                                    }
                                }
                            }
                            
                            if(genomeAnnotationInsertCount >= 100) {
                                annotationStatement.execute(genomeAnnotationSQLInsertStringBuilder.toString());

                                genomeAnnotationInsertCount = 0;
                                genomeAnnotationSQLInsertStringBuilder = new StringBuilder("INSERT INTO genome_annotation_srna_cluster (annotation_id, antisense, strand, sequence_id_text) VALUES ");
                            }

                            minAnnotationIndex = (annotationIndex + 1);

                            sequenceAnnotationReadsHashMap.put(annotationIndex, null);
                        }
                    }
                    
                    currentChromosome = mappingChromosome;
                    sequenceAnnotationHashMap = new HashMap();
                    sequenceAnnotationReadsHashMap = new HashMap();
                    minAnnotationIndex = -1;
                    
                    /* reduce processing to chromosomes, ignore Mt, Pt and additional contigs */
                    try {  
                        int chromosomeNumber = Integer.parseInt(currentChromosome);  
                    }  
                    catch(NumberFormatException e) {
                        continue;  
                    }  
                    
                    /* load current chromosome annotation data */
                    Statement annotationDataStatement = databaseConnection.getConnection().createStatement();
                    ResultSet annotationResultSet = annotationDataStatement.executeQuery("SELECT * FROM genome_annotation WHERE chromosome=" + currentChromosome + " ORDER BY start_position, end_position");
                    while(annotationResultSet.next()) {
                        int annotationIndex = annotationResultSet.getInt("annotation_id");
                        int endPosition = annotationResultSet.getInt("end_position");
                        int startPosition = annotationResultSet.getInt("start_position");
                        String sequenceType = annotationResultSet.getString("sequence_type");
                        String sequenceStrand = annotationResultSet.getString("strand");
                        
                        if(minAnnotationIndex == -1) {
                            minAnnotationIndex = annotationIndex;
                        }
                        
                        SequenceAnnotationType sequenceAnnotationType;
                        if(sequenceType.equals("gene")) {
                            sequenceAnnotationType = SequenceAnnotationType.GENE;
                        }
                        else if(sequenceType.equals("repeat")) {
                            sequenceAnnotationType = SequenceAnnotationType.REPEAT;
                        }
                        else {
                            sequenceAnnotationType = SequenceAnnotationType.INTERGENIC;
                        }
                        
                        sequenceAnnotationHashMap.put(annotationIndex, new SequenceAnnotation(annotationIndex, currentChromosome, sequenceAnnotationType, startPosition, endPosition, sequenceStrand));
                        sequenceAnnotationReadsHashMap.put(annotationIndex, new HashMap());
                        sequenceAnnotationReadsHashMap.get(annotationIndex).put("minus_sense", new ArrayList());
                        sequenceAnnotationReadsHashMap.get(annotationIndex).put("minus_antisense", new ArrayList());
                        sequenceAnnotationReadsHashMap.get(annotationIndex).put("plus_sense", new ArrayList());
                        sequenceAnnotationReadsHashMap.get(annotationIndex).put("plus_antisense", new ArrayList());
                    }
                }
                
                String annotationStrandDirection;
                int currentAnnotationIndex = minAnnotationIndex;
                SequenceAnnotation sequenceAnnotation;
                
                boolean annotationReachedFlag = false;
                
                while((sequenceAnnotation = sequenceAnnotationHashMap.get(currentAnnotationIndex)) != null) {
                    if((bamRecord.getAlignmentEnd() + 50) < sequenceAnnotation.getStartPosition()) {
                        for(int processedAnnotationIndex = finishedAnnotationIndex; processedAnnotationIndex < currentAnnotationIndex; processedAnnotationIndex++) {
                            SequenceAnnotation processedSequenceAnnotation = sequenceAnnotationHashMap.get(processedAnnotationIndex);
                            if(sequenceAnnotationReadsHashMap.get(processedAnnotationIndex) == null) {
                                continue;
                            }
                            
                            if(processedSequenceAnnotation.getEndPosition() < bamRecord.getAlignmentStart()) {
                                for(int strandIndex = 0; strandIndex <=1; strandIndex++) {
                                    for(int antisenseIndex = 0; antisenseIndex <= 1; antisenseIndex++) {
                                        if(sequenceAnnotationReadsHashMap.get(processedAnnotationIndex).get(strand[strandIndex] + "_" + antisense[antisenseIndex]).size() > 0) {
                                            StringBuilder readsStringBuilder = new StringBuilder();
                                            boolean firstReadFlag = true;
                                            for(String readName : sequenceAnnotationReadsHashMap.get(processedAnnotationIndex).get(strand[strandIndex] + "_" + antisense[antisenseIndex])) {
                                                if(!firstReadFlag) {
                                                    readsStringBuilder.append(";");
                                                }

                                                readsStringBuilder.append(readName);

                                                firstReadFlag = false;
                                            }

                                            if(genomeAnnotationInsertCount > 0) {
                                                genomeAnnotationSQLInsertStringBuilder.append(", ");
                                            }

                                            genomeAnnotationSQLInsertStringBuilder.append(" (" + processedAnnotationIndex + ", " + antisenseIndex + ", " + strandIndex + ", \";" + readsStringBuilder.toString() + ";\")");
                                            genomeAnnotationInsertCount++;
                                        }
                                    }
                                }
                                
                                sequenceAnnotationReadsHashMap.remove(processedAnnotationIndex);
                                finishedAnnotationIndex = processedAnnotationIndex;
                                
                                if(genomeAnnotationInsertCount >= 100) {
                                    annotationStatement.execute(genomeAnnotationSQLInsertStringBuilder.toString());

                                    genomeAnnotationInsertCount = 0;
                                    genomeAnnotationSQLInsertStringBuilder = new StringBuilder("INSERT INTO genome_annotation_srna_cluster (annotation_id, antisense, strand, sequence_id_text) VALUES ");
                                }
                                
                                minAnnotationIndex = (processedAnnotationIndex + 1);
                            }
                        }
                                                
                        annotationReachedFlag = true;
                                
                        break;
                    }
                    else if(bamRecord.getAlignmentStart() > sequenceAnnotationHashMap.get(currentAnnotationIndex).getEndPosition()) {
                        minAnnotationIndex = (currentAnnotationIndex + 1);
                    }
                    else {
                        if(bamRecord.getAlignmentStart() < sequenceAnnotation.getEndPosition()) {
                            if(bamRecord.getReadNegativeStrandFlag()) {
                                if(sequenceAnnotation.getStrand().equals("-") || (sequenceAnnotation.getStrand() == "*")) {
                                    annotationStrandDirection = "minus_sense";
                                }
                                else {
                                    annotationStrandDirection = "minus_antisense";
                                }
                            }
                            else {
                                if(sequenceAnnotation.getStrand().equals("+") || (sequenceAnnotation.getStrand() == "*")) {
                                    annotationStrandDirection = "plus_sense";
                                }
                                else {
                                    annotationStrandDirection = "plus_antisense";
                                }
                            }

                            if(sequenceAnnotationReadsHashMap.get(currentAnnotationIndex).get(annotationStrandDirection) == null) {
                                sequenceAnnotationReadsHashMap.get(currentAnnotationIndex).put(annotationStrandDirection, new ArrayList());
                            }
                            sequenceAnnotationReadsHashMap.get(currentAnnotationIndex).get(annotationStrandDirection).add(bamRecord.getReadName());
                        }
                    }

                    if(annotationReachedFlag)  {
                        break;
                    }
                    
                    currentAnnotationIndex++;
                }
            }
            
            for(int annotationIndex : sequenceAnnotationReadsHashMap.keySet()) {
                for(int strandIndex = 0; strandIndex <=1; strandIndex++) {
                    for(int antisenseIndex = 0; antisenseIndex <= 1; antisenseIndex++) {
                        if(sequenceAnnotationReadsHashMap.get(annotationIndex).get(strand[strandIndex] + "_" + antisense[antisenseIndex]).size() > 0) {
                            StringBuilder readsStringBuilder = new StringBuilder();
                            boolean firstReadFlag = true;
                            for(String readName : sequenceAnnotationReadsHashMap.get(annotationIndex).get(strand[strandIndex] + "_" + antisense[antisenseIndex])) {
                                if(!firstReadFlag) {
                                    readsStringBuilder.append(";");
                                }

                                readsStringBuilder.append(readName);

                                firstReadFlag = false;
                            }

                            if(genomeAnnotationInsertCount > 0) {
                                genomeAnnotationSQLInsertStringBuilder.append(", ");
                            }

                            genomeAnnotationSQLInsertStringBuilder.append(" (" + annotationIndex + ", " + antisenseIndex + ", " + strandIndex + ", \";" + readsStringBuilder.toString() + ";\")");
                            genomeAnnotationInsertCount++;
                        }
                    }
                }

                if(genomeAnnotationInsertCount >= 100) {
                    annotationStatement.execute(genomeAnnotationSQLInsertStringBuilder.toString());

                    genomeAnnotationInsertCount = 0;
                    genomeAnnotationSQLInsertStringBuilder = new StringBuilder("INSERT INTO genome_annotation_srna_cluster (annotation_id, antisense, strand, sequence_id_text) VALUES ");
                }
            }
            
            if(genomeAnnotationInsertCount > 0) {
                annotationStatement.execute(genomeAnnotationSQLInsertStringBuilder.toString());
            }
                        
            bamFileReader.close();
            
            annotationStatement.close();
        }
    }
    
    public static void main(String[] args) {
        String bamMappingFileName = null;
        String databaseUser = null;
        String databasePassword = null;
        String gffAnnotationFileName = null;
    
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-bamMappingFilename <file name>\tthe name of the BAM mapping file");
            System.out.println("-databaseUser <user>\tthe database user");
            System.out.println("-databasePassword <password>\tthe password for the database user");
            System.out.println("-gffAnnotationFilename <file name>\tthe name of the gff3 annotation file");
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
                    if(argumentTitle.equals("gffAnnotationFilename")) {
                        gffAnnotationFileName = argumentValue;
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
        
        if((bamMappingFileName == null) || (databaseUser == null) || (databasePassword == null) || (gffAnnotationFileName == null)) {
            System.out.println("Usage:");
            System.out.println("-bamMappingFilename <file name>\tthe name of the BAM mapping file");
            System.out.println("-databaseUser <user>\tthe database user");
            System.out.println("-databasePassword <password>\tthe password for the database user");
            System.out.println("-gffAnnotationFilename <file name>\tthe name of the gff3 annotation file");
            System.exit(1);
        }
        
        try {
            DatabaseAnnotationClusterGeneration databaseAnnotationClusterGeneration = new DatabaseAnnotationClusterGeneration(bamMappingFileName, gffAnnotationFileName, databaseUser, databasePassword);
            System.out.println("generate annotation data");
            databaseAnnotationClusterGeneration.generateAnnotation();
            System.out.println("generate cluster data");
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
    private final String gffAnnotationFileName;
}
