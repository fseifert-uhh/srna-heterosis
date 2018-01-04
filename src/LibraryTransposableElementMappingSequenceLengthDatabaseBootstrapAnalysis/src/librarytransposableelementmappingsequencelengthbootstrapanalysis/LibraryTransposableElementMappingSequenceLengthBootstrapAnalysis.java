package librarytransposableelementmappingsequencelengthbootstrapanalysis;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LibraryTransposableElementMappingSequenceLengthBootstrapAnalysis {
    public LibraryTransposableElementMappingSequenceLengthBootstrapAnalysis(DatabaseLoginData databaseLoginData) throws SQLException {
        this.databaseLoginData = databaseLoginData;
        
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        if(!databaseConnection.isActive()) {
            System.out.println("Error: unable to connect to the database");
            System.exit(1);
        }
        else {
            databaseConnection.close();
        }
        
        this.getMaximumReadNumber();
    }
    
    private void getMaximumReadNumber() throws SQLException {
        DatabaseConnection sequenceDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement();
        ResultSet sequenceDataResultSet = sequenceDataStatement.executeQuery("SELECT COUNT(sequence_id) FROM srna_sequence");
        if(sequenceDataResultSet.next()) {
            maximumReadNumber = sequenceDataResultSet.getInt("COUNT(sequence_id)");
        }
        sequenceDataResultSet.close();
        sequenceDataStatement.close();
        sequenceDataDatabaseConnection.close();
    }
    
    private HashMap<Integer,Integer> getSequenceMappingData(int minimalSequenceLength, int maximalSequenceLength, int annotationTypeIndex, int repeatClassIndex, int repeatOrderIndex, int repeatSuperFamilyIndex, int repeatFamilyIndex) throws SQLException {
        HashMap<Integer,Integer> annotationSequenceLengthHashMap = new HashMap();
        
        DatabaseConnection annotationClusterDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement annotationClusterDataStatement = annotationClusterDataDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        annotationClusterDataStatement.setFetchSize(Integer.MIN_VALUE);
        
        String annotationClassificationQuery = "";
        if(repeatFamilyIndex > 0) {
            annotationClassificationQuery = " AND genome_annotation_classification.repeat_family_id=" + repeatFamilyIndex;
        }
        else if(repeatSuperFamilyIndex > 0) {
            annotationClassificationQuery = " AND genome_annotation_classification.repeat_super_family_id=" + repeatSuperFamilyIndex;
        }
        else if(repeatOrderIndex > 0) {
            annotationClassificationQuery = " AND genome_annotation_classification.repeat_order_id=" + repeatOrderIndex;
        }
        else if(repeatClassIndex > 0) {
            annotationClassificationQuery = " AND genome_annotation_classification.repeat_class_id=" + repeatClassIndex;
        }
        else if(annotationTypeIndex > 0) {
            annotationClassificationQuery = " AND genome_annotation_classification.annotation_type_id=" + annotationTypeIndex;
        }
        
        //ArrayList<String> annotationSequenceIndexArrayList = new ArrayList();
        
        boolean[] annotationSequenceExpressionFlags = new boolean[maximumReadNumber];
        
        ResultSet annotationClusterDataResultSet = annotationClusterDataStatement.executeQuery("SELECT annotation_srna_cluster.* FROM genome_annotation_classification, annotation_srna_cluster WHERE genome_annotation_classification.annotation_id=annotation_srna_cluster.annotation_id" + annotationClassificationQuery);
        while(annotationClusterDataResultSet.next()) {        
            String[] sequenceIndices = annotationClusterDataResultSet.getString("sequence_id_text").split(";");
            
            for(String sequenceIndex : sequenceIndices) {
                if(!sequenceIndex.isEmpty()) {
                    annotationSequenceExpressionFlags[Integer.valueOf(sequenceIndex)] = true;
                }
            }
        }
        
        annotationClusterDataResultSet.close();
        annotationClusterDataStatement.close();
        annotationClusterDataDatabaseConnection.close();
        
        DatabaseConnection sequenceDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);
        ResultSet sequenceDataResultSet = sequenceDataStatement.executeQuery("SELECT * FROM srna_sequence");
        while(sequenceDataResultSet.next()) {
            int sequenceIndex = sequenceDataResultSet.getInt("sequence_id");

            if(annotationSequenceExpressionFlags[sequenceIndex]) {
                int sequenceLength = sequenceDataResultSet.getInt("length");

                if((sequenceLength >= minimalSequenceLength) && (sequenceLength <= maximalSequenceLength)) {
                    annotationSequenceLengthHashMap.put(sequenceIndex, sequenceLength);
                }
            }
        }
        sequenceDataResultSet.close();
        sequenceDataStatement.close();
        sequenceDataDatabaseConnection.close();
        
        return annotationSequenceLengthHashMap;
    }
    
    public HashMap<Integer,ArrayList<Integer>> sequenceBootstrapSubsetGeneration(String referenceSequenceSetFileName, int bootstrapNumber) throws IOException, SQLException {
        HashMap<Integer,ArrayList<Integer>> bootstrapSequenceSetHashMap = new HashMap();
        
        BufferedReader bufferedReader = new BufferedReader(new FileReader(referenceSequenceSetFileName));
        
        String sequenceIndexLine;
        while((sequenceIndexLine = bufferedReader.readLine()) != null) {
            if(!sequenceIndexLine.isEmpty()) {
                int sequenceIndex = Integer.valueOf(sequenceIndexLine);
                
                if(sequenceLengthHashMap.containsKey(sequenceIndex)) {
                    ArrayList bootstrapIndexArrayList = new ArrayList();
                    bootstrapIndexArrayList.add(0);
                    
                    bootstrapSequenceSetHashMap.put(sequenceIndex, bootstrapIndexArrayList);
                }
            }
        }
        
        HashMap<Integer,Integer> referenceLengthDistributionArrayList = new HashMap();
        for(int sequenceIndex : bootstrapSequenceSetHashMap.keySet()) {
            int sequenceCount = 1;
            if(referenceLengthDistributionArrayList.containsKey(sequenceLengthHashMap.get(sequenceIndex))) {
                sequenceCount += referenceLengthDistributionArrayList.get(sequenceLengthHashMap.get(sequenceIndex));
            }
            
            referenceLengthDistributionArrayList.put(sequenceLengthHashMap.get(sequenceIndex), sequenceCount);
        }
                
        bufferedReader.close();
        
        Random random = new Random();
        
        for(int bootstrapIndex = 1; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
            for(int sequenceLength : referenceLengthDistributionArrayList.keySet()) {
                int bootstrapSequenceSubsetSize = 0;
                
                ArrayList<Integer> mappingSequenceIndexArrayList = new ArrayList();
                int arrayIndex = 0;
                for(int sequenceIndex : sequenceLengthHashMap.keySet()) {
                    if(sequenceLengthHashMap.get(sequenceIndex) == sequenceLength) {
                        mappingSequenceIndexArrayList.add(sequenceIndex);
                    }
                }
                
                boolean[] boostrapRunMappingIndicesSelected = new boolean[sequenceLengthHashMap.size()];
                do {
                    int randomIndex = random.nextInt(mappingSequenceIndexArrayList.size());

                    if(boostrapRunMappingIndicesSelected[randomIndex]) {
                        continue;
                    }
                    
                    boostrapRunMappingIndicesSelected[randomIndex] = true;
                    
                    bootstrapSequenceSubsetSize++;
                } while(bootstrapSequenceSubsetSize < referenceLengthDistributionArrayList.get(sequenceLength));
                
                for(int selectedMappingIndex = 0; selectedMappingIndex < boostrapRunMappingIndicesSelected.length; selectedMappingIndex++) {
                    if(boostrapRunMappingIndicesSelected[selectedMappingIndex]) {
                        int sequenceIndex = mappingSequenceIndexArrayList.get(selectedMappingIndex);
                        
                        if(!bootstrapSequenceSetHashMap.containsKey(sequenceIndex)) {
                            bootstrapSequenceSetHashMap.put(sequenceIndex, new ArrayList());
                        }
                        
                        bootstrapSequenceSetHashMap.get(sequenceIndex).add(bootstrapIndex);
                    }
                }
            }
        }
        
        return bootstrapSequenceSetHashMap;
    }
    
    public void generateDatabaseAnnotationBootstrapSummary(String referenceSubsetFileName, int bootstrapNumber, int minimalSequenceLength, int maximalSequenceLength, int annotationTypeIndex, int repeatClassIndex, int repeatOrderIndex, int repeatSuperFamilyIndex, int repeatFamilyIndex) throws SQLException, IOException {
        ArrayList<String> inbredTitleArrayList = this.getInbredTitleList();

        System.out.print("annotation_type\trepeat_class\trepeat_order\trepeat_super_family\trepeat_family");

        if((minimalSequenceLength == 0) || (maximalSequenceLength == 0)) {
            for(int sequenceLength : sequenceLengthHashMap.values()) {
                if(minimalSequenceLength == 0) {
                    minimalSequenceLength = sequenceLength;
                    maximalSequenceLength = sequenceLength;
                }
                else if(sequenceLength < minimalSequenceLength) {
                    minimalSequenceLength = sequenceLength;
                }
                else if(sequenceLength > maximalSequenceLength) {
                    maximalSequenceLength = sequenceLength;
                }
            }
        }        
        int sequenceLengthCount = ((maximalSequenceLength - minimalSequenceLength) + 1);

        for(int bootstrapOutputIndex = 0; bootstrapOutputIndex <= 3; bootstrapOutputIndex++) {
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                System.out.print(((bootstrapOutputIndex == 0) ? "\treference_" : ((bootstrapOutputIndex == 1) ? "\tbootstrap_" : ((bootstrapOutputIndex == 2) ? "\tenrichment_pvalue_" : "\tdepletion_pvalue_"))) + (sequenceLengthIndex + minimalSequenceLength));
            }
        }
        System.out.println();
        
        this.sequenceLengthHashMap = this.getSequenceMappingData(minimalSequenceLength, maximalSequenceLength, annotationTypeIndex, repeatClassIndex, repeatOrderIndex, repeatSuperFamilyIndex, repeatFamilyIndex);
        
        HashMap<Integer,ArrayList<Integer>> bootstrapSequenceSubsetHashMap = this.sequenceBootstrapSubsetGeneration(referenceSubsetFileName, bootstrapNumber);
        
        Double[][] distinctReadCount = new Double[sequenceLengthCount][1 + bootstrapNumber];
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
            for(int bootstrapIndex = 0; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                distinctReadCount[sequenceLengthIndex][bootstrapIndex] = 0.0;
            }
        }

        DatabaseConnection sequenceExpressionDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceExpressionStatement = sequenceExpressionDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceExpressionStatement.setFetchSize(Integer.MIN_VALUE);
        ResultSet sequenceExpressionResultSet = sequenceExpressionStatement.executeQuery("SELECT * FROM srna_library_expression");
        while(sequenceExpressionResultSet.next()) {
            int sequenceIndex = sequenceExpressionResultSet.getInt("sequence_id");

            if(bootstrapSequenceSubsetHashMap.containsKey(sequenceIndex)) {
                ArrayList<Integer> bootstrapArrayList = bootstrapSequenceSubsetHashMap.get(sequenceIndex);

                for(int inbredIndex = 0; inbredIndex < inbredTitleArrayList.size(); inbredIndex++) {
                    double inbredExpression = sequenceExpressionResultSet.getDouble(inbredTitleArrayList.get(inbredIndex));

                    if(inbredExpression > 0) {
                        for(int bootstrapIndex = 0; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                            if(bootstrapArrayList.contains(bootstrapIndex)) {
                                distinctReadCount[sequenceLengthHashMap.get(sequenceIndex) - minimalSequenceLength][bootstrapIndex] += 1.0;//(double) sequenceMappingCountHashMap.get(sequenceIndex);
                            }
                        }

                        break;
                    }
                }
            }
        }
        
        double annotationGenomeKilobaseLength = 1000.0;

        DatabaseConnection annotationDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement annotationStatement = annotationDatabaseConnection.getConnection().createStatement();
        ResultSet annotationResultSet;
        
        if(repeatFamilyIndex > 0) {
            annotationResultSet = annotationStatement.executeQuery("SELECT SUM(genome_content_bp) FROM repeat_family_data WHERE family_id=" + repeatFamilyIndex);
        }
        else if(repeatSuperFamilyIndex > 0) {
            annotationResultSet = annotationStatement.executeQuery("SELECT SUM(genome_content_bp) FROM repeat_super_family_data WHERE super_family_id=" + repeatSuperFamilyIndex);
        }
        else if(repeatOrderIndex > 0) {
            annotationResultSet = annotationStatement.executeQuery("SELECT SUM(genome_content_bp) FROM repeat_order_data WHERE order_id=" + repeatOrderIndex);
        }
        else if(repeatClassIndex > 0) {
            annotationResultSet = annotationStatement.executeQuery("SELECT SUM(genome_content_bp) FROM repeat_class_data WHERE class_id=" + repeatClassIndex);
        }
        else if(annotationTypeIndex > 0) {
            annotationResultSet = annotationStatement.executeQuery("SELECT SUM(genome_content_bp) FROM annotation_type_data WHERE annotation_type_id=" + annotationTypeIndex);
        }
        else {
            annotationResultSet = annotationStatement.executeQuery("SELECT SUM(genome_content_bp) FROM annotation_type_data");
        }
            
        if(annotationResultSet.next()) {
            annotationGenomeKilobaseLength = annotationResultSet.getDouble("SUM(genome_content_bp)");

            if(annotationGenomeKilobaseLength < 1000) {
                annotationGenomeKilobaseLength = 1000.0;
            }
        }

        annotationGenomeKilobaseLength /= 1000.0;            
        
        StringBuilder annotationTitleStringBuilder = new StringBuilder();
        
        annotationResultSet = annotationStatement.executeQuery("SELECT title FROM annotation_type_data WHERE annotation_type_id=" + annotationTypeIndex);
        if(annotationResultSet.next()) {
            annotationTitleStringBuilder.append(annotationResultSet.getString("title"));
        }
        else {
            annotationTitleStringBuilder.append("all");
        }
        
        annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_class_data WHERE class_id=" + repeatClassIndex);
        if(annotationResultSet.next()) {
            annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
        }
        else {
            annotationTitleStringBuilder.append("\t");
        }
        
        annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_order_data WHERE order_id=" + repeatOrderIndex);
        if(annotationResultSet.next()) {
            annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
        }
        else {
            annotationTitleStringBuilder.append("\t");
        }
        
        annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_super_family_data WHERE super_family_id=" + repeatSuperFamilyIndex);
        if(annotationResultSet.next()) {
            annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
        }
        else {
            annotationTitleStringBuilder.append("\t");
        }        
        
        annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_family_data WHERE family_id=" + repeatFamilyIndex);
        if(annotationResultSet.next()) {
            annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
        }
        else {
            annotationTitleStringBuilder.append("\t");
        }        
        
        annotationResultSet.close();
        annotationStatement.close();
        annotationDatabaseConnection.close();

        System.out.println(annotationTitleStringBuilder.toString());
        
        StringBuilder depletionProbabilityStringBuilder = new StringBuilder();
        StringBuilder enrichmentProbabilityStringBuilder = new StringBuilder();

        for(int bootstrapOutputIndex = 0; bootstrapOutputIndex <= 1; bootstrapOutputIndex++) {
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                if(bootstrapOutputIndex == 0) {
                    System.out.print("\t" + (distinctReadCount[sequenceLengthIndex][0] / annotationGenomeKilobaseLength));
                }
                else {
                    double averageBootstrapDistinctReadCount = 0;
                    double depletionProbability = 1.0;
                    double enrichmentProbability = 1.0;

                    for(int bootstrapIndex = 1; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                        averageBootstrapDistinctReadCount += ((distinctReadCount[sequenceLengthIndex][bootstrapIndex] / annotationGenomeKilobaseLength) / (double) bootstrapNumber);

                        if(distinctReadCount[sequenceLengthIndex][0] < distinctReadCount[sequenceLengthIndex][bootstrapIndex]) {
                            depletionProbability += (1.0 / (double) bootstrapNumber);
                        }
                        if(distinctReadCount[sequenceLengthIndex][0] > distinctReadCount[sequenceLengthIndex][bootstrapIndex]) {
                            enrichmentProbability += (1.0 / (double) bootstrapNumber);
                        }
                    }

                    System.out.print("\t" + averageBootstrapDistinctReadCount);
                    depletionProbabilityStringBuilder.append("\t" + (1.0 - depletionProbability));
                    enrichmentProbabilityStringBuilder.append("\t" + (1.0 - enrichmentProbability));
                }
            }
        }

        System.out.print(enrichmentProbabilityStringBuilder.toString());
        System.out.print(depletionProbabilityStringBuilder.toString());
        System.out.println();
    }
        
    public void generateDatabaseFamilyAnnotationBootstrapSummary(String referenceSubsetFileName, int bootstrapNumber, int minimalSequenceLength, int maximalSequenceLength) throws SQLException, IOException {
        ArrayList<String> inbredTitleArrayList = this.getInbredTitleList();

        System.out.print("annotation_type\trepeat_class\trepeat_order\trepeat_super_family\trepeat_family");

        if((minimalSequenceLength == 0) || (maximalSequenceLength == 0)) {
            for(int sequenceLength : sequenceLengthHashMap.values()) {
                if(minimalSequenceLength == 0) {
                    minimalSequenceLength = sequenceLength;
                    maximalSequenceLength = sequenceLength;
                }
                else if(sequenceLength < minimalSequenceLength) {
                    minimalSequenceLength = sequenceLength;
                }
                else if(sequenceLength > maximalSequenceLength) {
                    maximalSequenceLength = sequenceLength;
                }
            }
        }        
        int sequenceLengthCount = ((maximalSequenceLength - minimalSequenceLength) + 1);

        for(int bootstrapOutputIndex = 0; bootstrapOutputIndex <= 3; bootstrapOutputIndex++) {
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                System.out.print(((bootstrapOutputIndex == 0) ? "\treference_" : ((bootstrapOutputIndex == 1) ? "\tbootstrap_" : ((bootstrapOutputIndex == 2) ? "\tenrichment_pvalue_" : "\tdepletion_pvalue_"))) + (sequenceLengthIndex + minimalSequenceLength));
            }
        }
        System.out.println();
        
        this.sequenceLengthHashMap = this.getSequenceMappingData(minimalSequenceLength, maximalSequenceLength, 3, 0, 0, 0, 0);

        HashMap<Integer,ArrayList<Integer>> bootstrapSequenceSubsetHashMap = this.sequenceBootstrapSubsetGeneration(referenceSubsetFileName, bootstrapNumber);

        double annotationGenomeKilobaseLength = 1000.0;

        int repeatFamilyIndex = 0;
        
        ArrayList<Integer> expressedSequenceIndexArrayList = new ArrayList();        
        DatabaseConnection sequenceExpressionDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceExpressionStatement = sequenceExpressionDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceExpressionStatement.setFetchSize(Integer.MIN_VALUE);
        ResultSet sequenceExpressionResultSet = sequenceExpressionStatement.executeQuery("SELECT * FROM srna_library_expression");
        while(sequenceExpressionResultSet.next()) {
            int sequenceIndex = sequenceExpressionResultSet.getInt("sequence_id");

            if(bootstrapSequenceSubsetHashMap.containsKey(sequenceIndex)) {
                for(int inbredIndex = 0; inbredIndex < inbredTitleArrayList.size(); inbredIndex++) {
                    double inbredExpression = sequenceExpressionResultSet.getDouble(inbredTitleArrayList.get(inbredIndex));

                    if(inbredExpression > 0) {
                        expressedSequenceIndexArrayList.add(sequenceIndex);

                        break;
                    }
                }
            }
        }
        
        DatabaseConnection annotationDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement annotationStatement = annotationDatabaseConnection.getConnection().createStatement();
        ResultSet annotationResultSet = null;

        DatabaseConnection familyAnnotationDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement familyAnnotationStatement = familyAnnotationDatabaseConnection.getConnection().createStatement();
        ResultSet familyAnnotationResultSet = familyAnnotationStatement.executeQuery("SELECT * FROM repeat_family_data");
        while(familyAnnotationResultSet.next()) {
            repeatFamilyIndex = familyAnnotationResultSet.getInt("family_id");
            annotationGenomeKilobaseLength = familyAnnotationResultSet.getDouble("genome_content_bp");

            if(annotationGenomeKilobaseLength < 1000) {
                annotationGenomeKilobaseLength = 1000.0;
            }

            annotationGenomeKilobaseLength /= 1000.0;            

            int annotationTypeIndex = 0;
            int repeatClassIndex = 0;
            int repeatOrderIndex = 0;
            int repeatSuperFamilyIndex = 0;
            annotationResultSet = annotationStatement.executeQuery("SELECT * FROM genome_annotation_classification WHERE repeat_family_id=" + repeatFamilyIndex + " LIMIT 1");
            if(annotationResultSet.next()) {
                annotationTypeIndex = annotationResultSet.getInt("annotation_type_id");
                repeatClassIndex = annotationResultSet.getInt("repeat_class_id");
                repeatOrderIndex = annotationResultSet.getInt("repeat_order_id");
                repeatSuperFamilyIndex = annotationResultSet.getInt("repeat_super_family_id");
            }
            annotationResultSet.close();

            StringBuilder annotationTitleStringBuilder = new StringBuilder();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM annotation_type_data WHERE annotation_type_id=" + annotationTypeIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append(annotationResultSet.getString("title"));
            }
            annotationResultSet.close();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_class_data WHERE class_id=" + repeatClassIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
            }
            annotationResultSet.close();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_order_data WHERE order_id=" + repeatOrderIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
            }
            annotationResultSet.close();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_super_family_data WHERE super_family_id=" + repeatSuperFamilyIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
            }
            annotationResultSet.close();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_family_data WHERE family_id=" + repeatFamilyIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
            }
            else {
                annotationTitleStringBuilder.append("\t");
            }        
            annotationResultSet.close();
            
            System.out.print(annotationTitleStringBuilder.toString());
            
            Double[][] distinctReadCount = new Double[sequenceLengthCount][1 + bootstrapNumber];
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                for(int bootstrapIndex = 0; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                    distinctReadCount[sequenceLengthIndex][bootstrapIndex] = 0.0;
                }
            }
            
            HashMap<Integer,Integer> annotationSequenceLengthHashMap = getSequenceMappingData(minimalSequenceLength, maximalSequenceLength, annotationTypeIndex, repeatClassIndex, repeatOrderIndex, repeatSuperFamilyIndex, repeatFamilyIndex);
            for(int sequenceIndex : annotationSequenceLengthHashMap.keySet()) {
                if(bootstrapSequenceSubsetHashMap.containsKey(sequenceIndex)) {
                    for(int bootstrapIndex : bootstrapSequenceSubsetHashMap.get(sequenceIndex)) {
                        distinctReadCount[annotationSequenceLengthHashMap.get(sequenceIndex) - minimalSequenceLength][bootstrapIndex] += 1.0;
                    }
                }
            }

            StringBuilder depletionProbabilityStringBuilder = new StringBuilder();
            StringBuilder enrichmentProbabilityStringBuilder = new StringBuilder();

            for(int bootstrapOutputIndex = 0; bootstrapOutputIndex <= 1; bootstrapOutputIndex++) {
                for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                    if(bootstrapOutputIndex == 0) {
                        System.out.print("\t" + (distinctReadCount[sequenceLengthIndex][0] / annotationGenomeKilobaseLength));
                    }
                    else {
                        double averageBootstrapDistinctReadCount = 0;
                        double depletionProbability = 1.0;
                        double enrichmentProbability = 1.0;

                        for(int bootstrapIndex = 1; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                            averageBootstrapDistinctReadCount += ((distinctReadCount[sequenceLengthIndex][bootstrapIndex] / annotationGenomeKilobaseLength) / (double) bootstrapNumber);

                            if(distinctReadCount[sequenceLengthIndex][0] < distinctReadCount[sequenceLengthIndex][bootstrapIndex]) {
                                depletionProbability += (1.0 / (double) bootstrapNumber);
                            }
                            if(distinctReadCount[sequenceLengthIndex][0] > distinctReadCount[sequenceLengthIndex][bootstrapIndex]) {
                                enrichmentProbability += (1.0 / (double) bootstrapNumber);
                            }
                        }

                        System.out.print("\t" + averageBootstrapDistinctReadCount);
                        depletionProbabilityStringBuilder.append("\t" + (1.0 - depletionProbability));
                        enrichmentProbabilityStringBuilder.append("\t" + (1.0 - enrichmentProbability));
                    }
                }
            }

            System.out.print(enrichmentProbabilityStringBuilder.toString());
            System.out.print(depletionProbabilityStringBuilder.toString());
            System.out.println();
        }

        familyAnnotationResultSet.close();
        familyAnnotationStatement.close();
        familyAnnotationDatabaseConnection.close();
        
        annotationStatement.close();
        annotationDatabaseConnection.close();
    }
    
    public void generateDatabaseSuperFamilyAnnotationBootstrapSummary(String referenceSubsetFileName, int bootstrapNumber, int minimalSequenceLength, int maximalSequenceLength) throws SQLException, IOException {
        ArrayList<String> inbredTitleArrayList = this.getInbredTitleList();

        System.out.print("annotation_type\trepeat_class\trepeat_order\trepeat_super_family");

        if((minimalSequenceLength == 0) || (maximalSequenceLength == 0)) {
            for(int sequenceLength : sequenceLengthHashMap.values()) {
                if(minimalSequenceLength == 0) {
                    minimalSequenceLength = sequenceLength;
                    maximalSequenceLength = sequenceLength;
                }
                else if(sequenceLength < minimalSequenceLength) {
                    minimalSequenceLength = sequenceLength;
                }
                else if(sequenceLength > maximalSequenceLength) {
                    maximalSequenceLength = sequenceLength;
                }
            }
        }        
        int sequenceLengthCount = ((maximalSequenceLength - minimalSequenceLength) + 1);

        for(int bootstrapOutputIndex = 0; bootstrapOutputIndex <= 3; bootstrapOutputIndex++) {
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                System.out.print(((bootstrapOutputIndex == 0) ? "\treference_" : ((bootstrapOutputIndex == 1) ? "\tbootstrap_" : ((bootstrapOutputIndex == 2) ? "\tenrichment_pvalue_" : "\tdepletion_pvalue_"))) + (sequenceLengthIndex + minimalSequenceLength));
            }
        }
        System.out.println();
        
        this.sequenceLengthHashMap = this.getSequenceMappingData(minimalSequenceLength, maximalSequenceLength, 3, 0, 0, 0, 0);

        HashMap<Integer,ArrayList<Integer>> bootstrapSequenceSubsetHashMap = this.sequenceBootstrapSubsetGeneration(referenceSubsetFileName, bootstrapNumber);

        double annotationGenomeKilobaseLength = 1000.0;

        int repeatSuperFamilyIndex = 0;
        
        ArrayList<Integer> expressedSequenceIndexArrayList = new ArrayList();        
        DatabaseConnection sequenceExpressionDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceExpressionStatement = sequenceExpressionDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceExpressionStatement.setFetchSize(Integer.MIN_VALUE);
        ResultSet sequenceExpressionResultSet = sequenceExpressionStatement.executeQuery("SELECT * FROM srna_library_expression");
        while(sequenceExpressionResultSet.next()) {
            int sequenceIndex = sequenceExpressionResultSet.getInt("sequence_id");

            if(bootstrapSequenceSubsetHashMap.containsKey(sequenceIndex)) {
                for(int inbredIndex = 0; inbredIndex < inbredTitleArrayList.size(); inbredIndex++) {
                    double inbredExpression = sequenceExpressionResultSet.getDouble(inbredTitleArrayList.get(inbredIndex));

                    if(inbredExpression > 0) {
                        expressedSequenceIndexArrayList.add(sequenceIndex);

                        break;
                    }
                }
            }
        }
        
        DatabaseConnection annotationDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement annotationStatement = annotationDatabaseConnection.getConnection().createStatement();
        ResultSet annotationResultSet = null;

        DatabaseConnection familyAnnotationDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement familyAnnotationStatement = familyAnnotationDatabaseConnection.getConnection().createStatement();
        ResultSet familyAnnotationResultSet = familyAnnotationStatement.executeQuery("SELECT * FROM repeat_super_family_data");
        while(familyAnnotationResultSet.next()) {
            repeatSuperFamilyIndex = familyAnnotationResultSet.getInt("super_family_id");
            annotationGenomeKilobaseLength = familyAnnotationResultSet.getDouble("genome_content_bp");

            if(annotationGenomeKilobaseLength < 1000) {
                annotationGenomeKilobaseLength = 1000.0;
            }

            annotationGenomeKilobaseLength /= 1000.0;            

            int annotationTypeIndex = 0;
            int repeatClassIndex = 0;
            int repeatOrderIndex = 0;
            annotationResultSet = annotationStatement.executeQuery("SELECT * FROM genome_annotation_classification WHERE repeat_super_family_id=" + repeatSuperFamilyIndex + " LIMIT 1");
            if(annotationResultSet.next()) {
                annotationTypeIndex = annotationResultSet.getInt("annotation_type_id");
                repeatClassIndex = annotationResultSet.getInt("repeat_class_id");
                repeatOrderIndex = annotationResultSet.getInt("repeat_order_id");
            }
            annotationResultSet.close();

            StringBuilder annotationTitleStringBuilder = new StringBuilder();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM annotation_type_data WHERE annotation_type_id=" + annotationTypeIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append(annotationResultSet.getString("title"));
            }
            annotationResultSet.close();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_class_data WHERE class_id=" + repeatClassIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
            }
            annotationResultSet.close();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_order_data WHERE order_id=" + repeatOrderIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
            }
            annotationResultSet.close();

            annotationResultSet = annotationStatement.executeQuery("SELECT title FROM repeat_super_family_data WHERE super_family_id=" + repeatSuperFamilyIndex);
            if(annotationResultSet.next()) {
                annotationTitleStringBuilder.append("\t" + annotationResultSet.getString("title"));
            }
            annotationResultSet.close();
            
            System.out.print(annotationTitleStringBuilder.toString());
            
            Double[][] distinctReadCount = new Double[sequenceLengthCount][1 + bootstrapNumber];
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                for(int bootstrapIndex = 0; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                    distinctReadCount[sequenceLengthIndex][bootstrapIndex] = 0.0;
                }
            }
            
            HashMap<Integer,Integer> annotationSequenceLengthHashMap = getSequenceMappingData(minimalSequenceLength, maximalSequenceLength, annotationTypeIndex, repeatClassIndex, repeatOrderIndex, repeatSuperFamilyIndex, 0);
            for(int sequenceIndex : annotationSequenceLengthHashMap.keySet()) {
                if(bootstrapSequenceSubsetHashMap.containsKey(sequenceIndex)) {
                    for(int bootstrapIndex : bootstrapSequenceSubsetHashMap.get(sequenceIndex)) {
                        distinctReadCount[annotationSequenceLengthHashMap.get(sequenceIndex) - minimalSequenceLength][bootstrapIndex] += 1.0;
                    }
                }
            }

            StringBuilder depletionProbabilityStringBuilder = new StringBuilder();
            StringBuilder enrichmentProbabilityStringBuilder = new StringBuilder();

            for(int bootstrapOutputIndex = 0; bootstrapOutputIndex <= 1; bootstrapOutputIndex++) {
                for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthCount; sequenceLengthIndex++) {
                    if(bootstrapOutputIndex == 0) {
                        System.out.print("\t" + (distinctReadCount[sequenceLengthIndex][0] / annotationGenomeKilobaseLength));
                    }
                    else {
                        double averageBootstrapDistinctReadCount = 0;
                        double depletionProbability = 0.0;
                        double enrichmentProbability = 0.0;

                        for(int bootstrapIndex = 1; bootstrapIndex <= bootstrapNumber; bootstrapIndex++) {
                            averageBootstrapDistinctReadCount += ((distinctReadCount[sequenceLengthIndex][bootstrapIndex] / annotationGenomeKilobaseLength) / (double) bootstrapNumber);

                            if(distinctReadCount[sequenceLengthIndex][0] < distinctReadCount[sequenceLengthIndex][bootstrapIndex]) {
                                depletionProbability += (1.0 / (double) bootstrapNumber);
                            }
                            if(distinctReadCount[sequenceLengthIndex][0] > distinctReadCount[sequenceLengthIndex][bootstrapIndex]) {
                                enrichmentProbability += (1.0 / (double) bootstrapNumber);
                            }
                        }

                        System.out.print("\t" + averageBootstrapDistinctReadCount);
                        depletionProbabilityStringBuilder.append("\t" + (1.0 - depletionProbability));
                        enrichmentProbabilityStringBuilder.append("\t" + (1.0 - enrichmentProbability));
                    }
                }
            }

            System.out.print(enrichmentProbabilityStringBuilder.toString());
            System.out.print(depletionProbabilityStringBuilder.toString());
            System.out.println();
        }

        familyAnnotationResultSet.close();
        familyAnnotationStatement.close();
        familyAnnotationDatabaseConnection.close();
        
        annotationStatement.close();
        annotationDatabaseConnection.close();
    }
    
    private ArrayList<String> getInbredTitleList() throws SQLException {
        ArrayList<String> inbredTitleList = new ArrayList();
        
        DatabaseConnection inbredTitleDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement inbredTitleStatement = inbredTitleDatabaseConnection.getConnection().createStatement();
        ResultSet inbredTitleResultSet = inbredTitleStatement.executeQuery("SELECT * FROM srna_libraries WHERE germplasm >= 0 AND germplasm <= 1 ORDER BY library_id ");
        while(inbredTitleResultSet.next()) {
            int inbredId = inbredTitleResultSet.getInt("library_id");
            String inbredTitle = inbredTitleResultSet.getString("library_title");
            
            inbredTitleList.add(inbredId, inbredTitle);
        }
        inbredTitleResultSet.close();
        inbredTitleStatement.close();
        inbredTitleDatabaseConnection.close();
        
        return inbredTitleList;
    }
    
    public HashMap<Integer,String> getAnnotationTypeTitles() throws SQLException {
        HashMap<Integer,String> annotationTypeHashMap = new HashMap();

        DatabaseConnection annotationTypeDatabaseConnection = new DatabaseConnection(databaseLoginData);
        if(!annotationTypeDatabaseConnection.isActive()) {
            System.out.println("Error: unable to connect to the database");
            System.exit(1);
        }

        Statement annotationTypeStatement = annotationTypeDatabaseConnection.getConnection().createStatement();
        ResultSet annotationTypeResultSet = annotationTypeStatement.executeQuery("SELECT * FROM annotation_type_data ORDER BY title");
        while(annotationTypeResultSet.next()) {
            int annotationTypeId = annotationTypeResultSet.getInt("annotation_type_id");
            String annotationTypeTitle = annotationTypeResultSet.getString("title");

            annotationTypeHashMap.put(annotationTypeId, annotationTypeTitle);
        }
        annotationTypeResultSet.close();
        annotationTypeStatement.close();
        annotationTypeDatabaseConnection.close();
        
        return annotationTypeHashMap;
    }
    
    public HashMap<Integer,String> getRepeatClassTitles() throws SQLException {
        HashMap<Integer,String> annotationClassHashMap = new HashMap();

        DatabaseConnection annotationClassDatabaseConnection = new DatabaseConnection(databaseLoginData);
        if(!annotationClassDatabaseConnection.isActive()) {
            System.out.println("Error: unable to connect to the database");
            System.exit(1);
        }            

        Statement annotationClassStatement = annotationClassDatabaseConnection.getConnection().createStatement();
        ResultSet annotationClassResultSet = annotationClassStatement.executeQuery("SELECT * FROM repeat_class_data ORDER BY title");
        while(annotationClassResultSet.next()) {
            int annotationClassId = annotationClassResultSet.getInt("class_id");
            String annotationClassTitle = annotationClassResultSet.getString("title");

            annotationClassHashMap.put(annotationClassId, annotationClassTitle);
        }
        annotationClassResultSet.close();
        annotationClassStatement.close();
        annotationClassDatabaseConnection.close();
        
        return annotationClassHashMap;
    }
    
    public HashMap<Integer,String> getRepeatFamilyTitles(int repeatSuperFamilyIndex) throws SQLException {
        HashMap<Integer,String> annotationFamilyHashMap = new HashMap();

        DatabaseConnection annotationFamilyDatabaseConnection = new DatabaseConnection(databaseLoginData);
        if(!annotationFamilyDatabaseConnection.isActive()) {
            System.out.println("Error: unable to connect to the database");
            System.exit(1);
        }            

        Statement annotationFamilyStatement = annotationFamilyDatabaseConnection.getConnection().createStatement();
        ResultSet annotationFamilyResultSet = annotationFamilyStatement.executeQuery("SELECT * FROM repeat_family_data WHERE super_family_id=" + repeatSuperFamilyIndex + " ORDER BY title");
        while(annotationFamilyResultSet.next()) {
            int annotationFamilyId = annotationFamilyResultSet.getInt("family_id");
            String annotationFamilyTitle = annotationFamilyResultSet.getString("title");

            annotationFamilyHashMap.put(annotationFamilyId, annotationFamilyTitle);
        }
        annotationFamilyResultSet.close();
        annotationFamilyStatement.close();
        annotationFamilyDatabaseConnection.close();
        
        return annotationFamilyHashMap;
    }
    
    public HashMap<Integer,String> getRepeatOrderTitles(int repeatClassIndex) throws SQLException {
        HashMap<Integer,String> annotationOrderHashMap = new HashMap();

        DatabaseConnection annotationOrderDatabaseConnection = new DatabaseConnection(databaseLoginData);
        if(!annotationOrderDatabaseConnection.isActive()) {
            System.out.println("Error: unable to connect to the database");
            System.exit(1);
        }            

        Statement annotationOrderStatement = annotationOrderDatabaseConnection.getConnection().createStatement();
        ResultSet annotationOrderResultSet = annotationOrderStatement.executeQuery("SELECT * FROM repeat_order_data WHERE class_id=" + repeatClassIndex + " ORDER BY title");
        while(annotationOrderResultSet.next()) {
            int annotationOrderId = annotationOrderResultSet.getInt("order_id");
            String annotationOrderTitle = annotationOrderResultSet.getString("title");

            annotationOrderHashMap.put(annotationOrderId, annotationOrderTitle);
        }
        annotationOrderResultSet.close();
        annotationOrderStatement.close();
        annotationOrderDatabaseConnection.close();
        
        return annotationOrderHashMap;
    }
    
    public HashMap<Integer,String> getRepeatSuperFamilyTitles(int repeatOrderIndex) throws SQLException {
        HashMap<Integer,String> annotationSuperFamilyHashMap = new HashMap();

        DatabaseConnection annotationSuperFamilyDatabaseConnection = new DatabaseConnection(databaseLoginData);
        if(!annotationSuperFamilyDatabaseConnection.isActive()) {
            System.out.println("Error: unable to connect to the database");
            System.exit(1);
        }            

        Statement annotationSuperFamilyStatement = annotationSuperFamilyDatabaseConnection.getConnection().createStatement();
        ResultSet annotationSuperFamilyResultSet = annotationSuperFamilyStatement.executeQuery("SELECT * FROM repeat_super_family_data WHERE order_id=" + repeatOrderIndex + " ORDER BY title");
        while(annotationSuperFamilyResultSet.next()) {
            int annotationSuperFamilyId = annotationSuperFamilyResultSet.getInt("super_family_id");
            String annotationSuperFamilyTitle = annotationSuperFamilyResultSet.getString("title");

            annotationSuperFamilyHashMap.put(annotationSuperFamilyId, annotationSuperFamilyTitle);
        }
        annotationSuperFamilyResultSet.close();
        annotationSuperFamilyStatement.close();
        annotationSuperFamilyDatabaseConnection.close();
        
        return annotationSuperFamilyHashMap;
    }
    
    public HashMap<Integer,String> getLibraryTitles() throws SQLException {
        if(libraryTitleHashMap == null) {
            libraryTitleHashMap = new HashMap();

            DatabaseConnection libraryTitleDatabaseConnection = new DatabaseConnection(databaseLoginData);
            if(!libraryTitleDatabaseConnection.isActive()) {
                System.out.println("Error: unable to connect to the database");
                System.exit(1);
            }
            
            Statement libraryTitleStatement = libraryTitleDatabaseConnection.getConnection().createStatement();
            ResultSet libraryTitleResultSet = libraryTitleStatement.executeQuery("SELECT * FROM srna_libraries");
            while(libraryTitleResultSet.next()) {
                int libraryId = libraryTitleResultSet.getInt("library_id");
                String libraryTitle = libraryTitleResultSet.getString("library_title");

                libraryTitleHashMap.put(libraryId, libraryTitle);
            }
            libraryTitleResultSet.close();
            libraryTitleStatement.close();
            libraryTitleDatabaseConnection.close();
        }
        
        return libraryTitleHashMap;
    }

    public static void main(String[] args) {
        String databaseUser = null;
        String databasePassword = null;
        String sequenceSubsetIndicesFileName = null;
        
        int bootstrapNumber = 1000;

        int minimalSequenceLength = 18;
        int maximalSequenceLength = 28;
        
        String classificationType = "family";
        
        if(args.length == 0) {
            System.out.println("-bootstrap <number of runs>");
            System.out.println("-classification <type>\tsuper - repeat super-families, family  - repeat families");
            System.out.println("-filename <path/filename> \tfile containing sRNA indices (one per row)");
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
                    
                    if(argumentTitle.equals("bootstrap")) {
                        bootstrapNumber = Integer.valueOf(argumentValue);
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                    if(argumentTitle.equals("filename")) {
                        sequenceSubsetIndicesFileName = argumentValue;
                    }
                    if(argumentTitle.equals("classification")) {
                        if(argumentValue.equals("super")) {
                            classificationType = "super";
                        }
                        else if(argumentValue.equals("family")) {
                        }
                    }
                }
            }
            
            if((sequenceSubsetIndicesFileName == null) || (databaseUser == null) || (databasePassword == null)) {
            System.out.println("-bootstrap <number of runs>");
                System.out.println("-classification <type>\tsuper -> repeat super-families, family -> repeat families");
                System.out.println("-filename <path/filename> \tfile containing sRNA indices (one per row)");
                System.out.println("-databaseUser <username>");
                System.out.println("-databasePassword <password>");
                System.exit(1);
            }
        }
        
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
            
            LibraryTransposableElementMappingSequenceLengthBootstrapAnalysis libraryAnnotationMappingDatabaseAnalysis = new LibraryTransposableElementMappingSequenceLengthBootstrapAnalysis(databaseLoginData);
            
            if(classificationType.equals("family")) {
                libraryAnnotationMappingDatabaseAnalysis.generateDatabaseFamilyAnnotationBootstrapSummary(sequenceSubsetIndicesFileName, bootstrapNumber, minimalSequenceLength, maximalSequenceLength);
            }
            else if(classificationType.equals("super")) {
                libraryAnnotationMappingDatabaseAnalysis.generateDatabaseSuperFamilyAnnotationBootstrapSummary(sequenceSubsetIndicesFileName, bootstrapNumber, minimalSequenceLength, maximalSequenceLength);
            }
            
        } catch(IOException e) {
            System.out.println(e);
        } catch(SQLException e) {
            System.out.println(e);
        }
    }
   
    private int maximumReadNumber;
    
    private final DatabaseLoginData databaseLoginData;
    
    private HashMap<Integer,Integer> sequenceLengthHashMap;
    private HashMap<Integer,String> libraryTitleHashMap = null;
}
