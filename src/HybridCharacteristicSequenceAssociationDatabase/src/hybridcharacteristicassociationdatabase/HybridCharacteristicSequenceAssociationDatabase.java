package hybridcharacteristicassociationdatabase;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import de.uni_hamburg.fseifert.statistics.MultipleTestingCorrection;
import de.uni_hamburg.fseifert.statistics.MultipleTestingCorrectionMethod;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import jsc.datastructures.PairedData;
import jsc.regression.PearsonCorrelation;

public class HybridCharacteristicSequenceAssociationDatabase {
    public HybridCharacteristicSequenceAssociationDatabase(DatabaseLoginData databaseLoginData, MultipleTestingCorrectionMethod multipleTestingCorrectionMethod) throws SQLException {
        databaseConnection = new DatabaseConnection(databaseLoginData);
        if(databaseConnection.isActive()) {
            this.loadHybridInbredParentIdentifiers();
            this.loadInbredTitles();
        }
        
        this.multipleTestingCorrectionMethod = multipleTestingCorrectionMethod;
    }
    
    public void close() throws SQLException {
        databaseConnection.close();
    }
    
    public HashMap<Integer,Boolean> getHybridInbredParentDifferentialExpression(HashMap<Integer,Double> inbredParentExpressionHashMap, double minExpressionThreshold, double minFoldChangeThreshold) {
        HashMap<Integer,Boolean> hybridInbredParentDifferentialExpressionFlagHashMap = new HashMap();
        
        for(int hybridIndex : hybridInbredParentIndicesHashMap.keySet()) {
            double lowInbredParentExpression = Math.min(inbredParentExpressionHashMap.get(hybridInbredParentIndicesHashMap.get(hybridIndex)[0]), inbredParentExpressionHashMap.get(hybridInbredParentIndicesHashMap.get(hybridIndex)[1]));
            double highInbredParentExpression = Math.max(inbredParentExpressionHashMap.get(hybridInbredParentIndicesHashMap.get(hybridIndex)[0]), inbredParentExpressionHashMap.get(hybridInbredParentIndicesHashMap.get(hybridIndex)[1]));

            hybridInbredParentDifferentialExpressionFlagHashMap.put(hybridIndex, false);
            
            if(minExpressionThreshold > 0) {
                if(lowInbredParentExpression < minExpressionThreshold) {
                    if(highInbredParentExpression >= (minExpressionThreshold * minFoldChangeThreshold)) {
                        hybridInbredParentDifferentialExpressionFlagHashMap.put(hybridIndex, true);
                    }
                }
                else if((highInbredParentExpression / lowInbredParentExpression) >= minFoldChangeThreshold) {
                    hybridInbredParentDifferentialExpressionFlagHashMap.put(hybridIndex, true);
                }
            }
            else {
                if(lowInbredParentExpression == 0) {
                    if(highInbredParentExpression >= (minFoldChangeThreshold * minExpressionThreshold)) {
                        hybridInbredParentDifferentialExpressionFlagHashMap.put(hybridIndex, true);
                    }
                }
            }
        }
        
        return hybridInbredParentDifferentialExpressionFlagHashMap;
    }

    public HashMap<Integer,Double> getAssociatedSequences(HybridCharacteristic hybridCharacteristic, SequenceMappingStatus sequenceMappingStatus, String startCharacter, int sequenceLength, double minExpressionThreshold, double minFoldChange, int groupBalance, double alphaError) throws SQLException {
        HashMap<Integer,Double> putativeAssociatedSequences = new HashMap();

        double minPValue = 1;
        
        if(databaseConnection.isActive()) {
            HashMap<Integer,Double> hybridCharacteristicValueHashMap = getHybridCharacteristicValues(hybridCharacteristic);
            
            HybridCharacteristicAssociationStatistic hybridCharacteristicAssociationStatistic = new HybridCharacteristicAssociationStatistic(hybridCharacteristicValueHashMap, groupBalance);
            
            String mappingStatusQuery = "";
            switch(sequenceMappingStatus) {
                case MAPPED:
                    mappingStatusQuery = " AND srna_mapping_status.mapping_count>=1 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                    break;
                case UNMAPPED:
                    mappingStatusQuery = " AND srna_mapping_status.mapping_count=0 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                    break;
                case SINGLE_LOCI:
                    mappingStatusQuery = " AND srna_mapping_status.mapping_count=1 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                    break;
                case MULTIPLE_LOCI:
                    mappingStatusQuery = " AND srna_mapping_status.mapping_count>1 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                    break;
                case ALL:
                default:
                    mappingStatusQuery = " AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";
            }

            int sequenceCount = 0;        
            Statement sequenceExpressionStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            sequenceExpressionStatement.setFetchSize(Integer.MIN_VALUE);

            String sqlSequenceLengthConstraint = " AND srna_sequence.length>=18 and srna_sequence.length<=28";
            if(sequenceLength > 0) {
                sqlSequenceLengthConstraint = " AND srna_sequence.length=" + sequenceLength;
            }
            
            String sqlStartCharacterConstraint = "";
            if(!startCharacter.isEmpty()) {
                sqlStartCharacterConstraint = " AND sequence LIKE \"" + startCharacter + "%\"";
            }
            
            ResultSet sequenceExpressionResultSet = sequenceExpressionStatement.executeQuery("SELECT srna_library_expression.* FROM srna_library_expression, srna_mapping_status, srna_sequence WHERE srna_library_expression.sequence_id=srna_sequence.sequence_id " + mappingStatusQuery + sqlSequenceLengthConstraint + sqlStartCharacterConstraint);
            while(sequenceExpressionResultSet.next()) {
                int sequenceIndex = sequenceExpressionResultSet.getInt("sequence_id");
                
                boolean sequenceExpressionFlag = false;
                
                HashMap<Integer,Double> inbredSequenceExpressionHashMap = new HashMap();
                for(int inbredIndex : inbredTitleHashMap.keySet()) {
                    double inbredLineSequenceExpression = sequenceExpressionResultSet.getDouble(inbredTitleHashMap.get(inbredIndex));
                    
                    if(inbredLineSequenceExpression > 0) {
                        sequenceExpressionFlag = true;
                    }
                    
                    inbredSequenceExpressionHashMap.put(inbredIndex, inbredLineSequenceExpression);
                }

                if(sequenceExpressionFlag) {
                    HashMap<Integer,Boolean> hybridParentDifferentialExpressionFlagHashMap = getHybridInbredParentDifferentialExpression(inbredSequenceExpressionHashMap, minExpressionThreshold, minFoldChange);
                    for(int hybridIndex : hybridInbredParentIndicesHashMap.keySet()) {
                        if(hybridParentDifferentialExpressionFlagHashMap.get(hybridIndex)) {
                            sequenceCount++;

                            break;
                        }
                    }

                    double sequenceAssociationProbability = hybridCharacteristicAssociationStatistic.getStatistic(hybridParentDifferentialExpressionFlagHashMap);

                    if(Math.abs(sequenceAssociationProbability) < minPValue) {
                        minPValue = Math.abs(sequenceAssociationProbability);
                    }
                    
                    if(Math.abs(sequenceAssociationProbability) <= alphaError) {
                        putativeAssociatedSequences.put(sequenceIndex, sequenceAssociationProbability);
                    }
                }
            }
            sequenceExpressionResultSet.close();
            sequenceExpressionStatement.close();
            
            System.out.print(sequenceCount + ";" + minPValue + ";");
            
            if(putativeAssociatedSequences.size() > 0) {
                double[] candidateProbabilities = new double[putativeAssociatedSequences.size()];            
                int probabilityIndex = 0;
                for(double probabilityValue : putativeAssociatedSequences.values()) {
                    candidateProbabilities[probabilityIndex] = Math.abs(probabilityValue);
                    
                    probabilityIndex++;
                }
                
                MultipleTestingCorrection multipleTestingCorrection = new MultipleTestingCorrection(candidateProbabilities, sequenceCount);
                double multipleTestingCorrectedAlphaError = multipleTestingCorrection.getCorrectedAlphaErrorThreshold(multipleTestingCorrectionMethod, alphaError);
                System.out.print(multipleTestingCorrectedAlphaError + ";");
                
                Object[] candidateSequenceIds = putativeAssociatedSequences.keySet().toArray();
                for(int candidateIndex = 0; candidateIndex < candidateSequenceIds.length; candidateIndex++) {
                    if(Math.abs(putativeAssociatedSequences.get((Integer) candidateSequenceIds[candidateIndex])) > multipleTestingCorrectedAlphaError) {
                        putativeAssociatedSequences.remove((Integer) candidateSequenceIds[candidateIndex]);
                    }
                }
            }
        }
        
        return putativeAssociatedSequences;
    }
    
    private void loadInbredTitles() throws SQLException {
        inbredTitleHashMap = new HashMap();
        
        Statement inbredTitleStatement = databaseConnection.getConnection().createStatement();
        ResultSet inbredTitleResultSet = inbredTitleStatement.executeQuery("SELECT * FROM srna_libraries order by library_id");
        while(inbredTitleResultSet.next()) {
            int inbredId = inbredTitleResultSet.getInt("library_id");
            String inbredTitle = inbredTitleResultSet.getString("library_title");
            
            inbredTitleHashMap.put(inbredId, inbredTitle);
        }
        inbredTitleResultSet.close();
        inbredTitleStatement.close();
    }
    
    private HashMap<Integer,Double> getHybridCharacteristicValues(HybridCharacteristic hybridCharacteristic) throws SQLException {
        HashMap<Integer,Double> hybridCharacteristicValueHashMap = new HashMap();
        
        Statement hybridCharacteristicStatement = databaseConnection.getConnection().createStatement();
        
        ResultSet hybridCharacteristicResultSet = hybridCharacteristicStatement.executeQuery("SELECT * FROM hybrid_field_data_gy");
        while(hybridCharacteristicResultSet.next()) {
            int hybridIndex = hybridCharacteristicResultSet.getInt("hybrid_id");

            double hybridCharacteristicValue;
            switch(hybridCharacteristic) {
                case BEST_PARENT_HETEROSIS:
                    hybridCharacteristicValue = hybridCharacteristicResultSet.getDouble("best_parent_heterosis_r");
                    break;
                case HYBRID_PERFORMANCE:
                    hybridCharacteristicValue = hybridCharacteristicResultSet.getDouble("hybrid_performance");
                    break;
                case MID_PARENT_HETEROSIS:
                default:
                    hybridCharacteristicValue = hybridCharacteristicResultSet.getDouble("mid_parent_heterosis_r");
            }
        
            hybridCharacteristicValueHashMap.put(hybridIndex, hybridCharacteristicValue);
        }

        hybridCharacteristicResultSet.close();
        hybridCharacteristicStatement.close();
        
        return hybridCharacteristicValueHashMap;
    }
    
    private void loadHybridInbredParentIdentifiers() throws SQLException {
        hybridInbredParentIndicesHashMap = new HashMap();
        
        Statement hybridParentsStatement = databaseConnection.getConnection().createStatement();
        ResultSet hybridParentsResultSet = hybridParentsStatement.executeQuery("SELECT * FROM hybrid_inbred_pairs ORDER BY hybrid_id");
        while(hybridParentsResultSet.next()) {
            int hybridIndex = hybridParentsResultSet.getInt("hybrid_id");
            
            Integer[] hybridInbredParentIndices = new Integer[2];
            hybridInbredParentIndices[0] = hybridParentsResultSet.getInt("parent1_id");
            hybridInbredParentIndices[1] = hybridParentsResultSet.getInt("parent2_id");
            
            hybridInbredParentIndicesHashMap.put(hybridIndex, hybridInbredParentIndices);
        }
        hybridParentsResultSet.close();
        hybridParentsStatement.close();
    }
    
    public void performCorrelation(HybridCharacteristic hybridCharacteristic, HashMap<Integer,HashMap<Integer,Double>>[] inbredElementExpressionHashMap, double minExpressionThreshold, double minFoldChangeThreshold) throws SQLException {
        if(inbredElementExpressionHashMap[0].isEmpty() && inbredElementExpressionHashMap[1].isEmpty()) {
            return;
        }
        
        HashMap<Integer,Double> hybridCharacteristicValueHashMap = getHybridCharacteristicValues(hybridCharacteristic);
        
        HashMap<Integer,Double> hybridBinaryDistanceHashMap = new HashMap();
        HashMap<Integer,Double> hybridEuclideanDistanceHashMap = new HashMap();

        HashMap<Integer,Double>[] binaryDistanceHashMap = new HashMap[2];
        binaryDistanceHashMap[0] = new HashMap();
        binaryDistanceHashMap[1] = new HashMap();
        HashMap<Integer,Double>[] euclideanDistanceHashMap = new HashMap[2];
        euclideanDistanceHashMap[0] = new HashMap();
        euclideanDistanceHashMap[1] = new HashMap();

        for(int associationTypeIndex = 0; associationTypeIndex < 2; associationTypeIndex++) {
            for(int inbredElementIndex : inbredElementExpressionHashMap[associationTypeIndex].keySet()) {
                HashMap<Integer,Boolean> hybridInbredParentDifferentialExpression = getHybridInbredParentDifferentialExpression(inbredElementExpressionHashMap[associationTypeIndex].get(inbredElementIndex), minExpressionThreshold, minFoldChangeThreshold);

                for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
                    if(!euclideanDistanceHashMap[associationTypeIndex].containsKey(hybridIndex)) {
                        binaryDistanceHashMap[associationTypeIndex].put(hybridIndex, 0.0);
                        euclideanDistanceHashMap[associationTypeIndex].put(hybridIndex, 0.0);
                    }

                    double lowParentExpression = Math.min(inbredElementExpressionHashMap[associationTypeIndex].get(inbredElementIndex).get(hybridInbredParentIndicesHashMap.get(hybridIndex)[0]), inbredElementExpressionHashMap[associationTypeIndex].get(inbredElementIndex).get(hybridInbredParentIndicesHashMap.get(hybridIndex)[1]));
                    double highParentExpression = Math.max(inbredElementExpressionHashMap[associationTypeIndex].get(inbredElementIndex).get(hybridInbredParentIndicesHashMap.get(hybridIndex)[0]), inbredElementExpressionHashMap[associationTypeIndex].get(inbredElementIndex).get(hybridInbredParentIndicesHashMap.get(hybridIndex)[1]));

                    if(hybridInbredParentDifferentialExpression.get(hybridIndex)) {
                        binaryDistanceHashMap[associationTypeIndex].put(hybridIndex, binaryDistanceHashMap[associationTypeIndex].get(hybridIndex) + 1.0);
                    }

                    euclideanDistanceHashMap[associationTypeIndex].put(hybridIndex, euclideanDistanceHashMap[associationTypeIndex].get(hybridIndex) + Math.pow((highParentExpression - lowParentExpression), 2));
                }
            }
        }

        System.out.println();
        System.out.println("correlation: ");
        
        if(!binaryDistanceHashMap[0].isEmpty() || !binaryDistanceHashMap[1].isEmpty()) {
            System.out.println("hybrid_id;binary_distance;euclidean_distance;trait_value;n_pos_associated;n_neg_associated");
            
            for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
                hybridBinaryDistanceHashMap.put(hybridIndex, (Math.sqrt(binaryDistanceHashMap[0].get(hybridIndex) / (double) inbredElementExpressionHashMap[0].size()) * ((double) inbredElementExpressionHashMap[0].size() / ((double) inbredElementExpressionHashMap[0].size() + (double) inbredElementExpressionHashMap[1].size()))) + ((1.0 - Math.sqrt(binaryDistanceHashMap[1].get(hybridIndex) / (double) inbredElementExpressionHashMap[1].size())) * ((double) inbredElementExpressionHashMap[1].size() / ((double) inbredElementExpressionHashMap[0].size() + (double) inbredElementExpressionHashMap[1].size()))));
                hybridEuclideanDistanceHashMap.put(hybridIndex, (Math.sqrt(euclideanDistanceHashMap[0].get(hybridIndex) + Math.sqrt(euclideanDistanceHashMap[1].get(hybridIndex)))));
               
                System.out.println(hybridIndex + ";"  + hybridBinaryDistanceHashMap.get(hybridIndex) + ";" + hybridEuclideanDistanceHashMap.get(hybridIndex) + ";" + hybridCharacteristicValueHashMap.get(hybridIndex) + ";" + binaryDistanceHashMap[0].get(hybridIndex) + ";" + binaryDistanceHashMap[1].get(hybridIndex));
            }
        
            double[] hybridBinaryDistance = new double[hybridCharacteristicValueHashMap.size()];
            double[] hybridCharacteristicValues = new double[hybridCharacteristicValueHashMap.size()];
            double[] hybridEuclideanDistance = new double[hybridCharacteristicValueHashMap.size()];

            int arrayIndex = 0;
            for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
                hybridBinaryDistance[arrayIndex] = hybridBinaryDistanceHashMap.get(hybridIndex);
                hybridCharacteristicValues[arrayIndex] = hybridCharacteristicValueHashMap.get(hybridIndex);
                hybridEuclideanDistance[arrayIndex] = hybridEuclideanDistanceHashMap.get(hybridIndex);

                arrayIndex++;
            }

            PearsonCorrelation binaryDistanceHybridCharacteristicCorrelation = new PearsonCorrelation(new PairedData(hybridBinaryDistance, hybridCharacteristicValues));
            PearsonCorrelation euclideanDistanceHybridCharacteristicCorrelation = new PearsonCorrelation(new PairedData(hybridEuclideanDistance, hybridCharacteristicValues));

            System.out.print(minExpressionThreshold + ";" + minFoldChangeThreshold + ";pos: " + inbredElementExpressionHashMap[0].size() + ";neg: " + inbredElementExpressionHashMap[1].size() + ";R(bin): " + binaryDistanceHybridCharacteristicCorrelation.getR() + ";R(eucl): " + euclideanDistanceHybridCharacteristicCorrelation.getR());
        }
    }
    
    public void testSequences(DatabaseLoginData databaseLoginData, HybridCharacteristic hybridCharacteristic, int sequenceLength, double minExpressionThreshold, double minFoldChangeThreshold, SequenceMappingStatus sequenceMappingStatus, int groupBalance, double alphaError, boolean mappingCountCorrection) throws SQLException {
        DatabaseConnection inbredElementExpressionDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement inbredElementExpressionStatement = inbredElementExpressionDatabaseConnection.getConnection().createStatement();
        
        HashMap<Integer,Double> midParentalHeterosisAssociatedSequencesHashMap = getAssociatedSequences(hybridCharacteristic, sequenceMappingStatus, "", sequenceLength, minExpressionThreshold, minFoldChangeThreshold, groupBalance, alphaError);

        HashMap<Integer,HashMap<Integer,Double>>[] inbredElementExpressionHashMap = new HashMap[2];
        inbredElementExpressionHashMap[0] = new HashMap(); // positive association
        inbredElementExpressionHashMap[1] = new HashMap(); // negative association
        for(int inbredElementIndex : midParentalHeterosisAssociatedSequencesHashMap.keySet()) {
            int associationTypeIndex = 0;
            if(midParentalHeterosisAssociatedSequencesHashMap.get(inbredElementIndex) < 0) {
                associationTypeIndex = 1;
            }
            
            inbredElementExpressionHashMap[associationTypeIndex].put(inbredElementIndex, new HashMap());
            
            ResultSet inbredElementExpressionResultSet = inbredElementExpressionStatement.executeQuery("SELECT * FROM srna_library_expression WHERE sequence_id=" + inbredElementIndex);
            if(inbredElementExpressionResultSet.next()) {
                int mappingCountCorrectionFactor = 1;
                if(mappingCountCorrection) {
                    DatabaseConnection inbredElementMappingCountDatabaseConnection = new DatabaseConnection(databaseLoginData);
                    Statement inbredElementMappingCountStatement = inbredElementMappingCountDatabaseConnection.getConnection().createStatement();
                    ResultSet inbredElementMappingCountResultSet = inbredElementMappingCountStatement.executeQuery("SELECT mapping_count FROM srna_mapping_status WHERE sequence_id=" + inbredElementIndex);

                    inbredElementMappingCountResultSet.next();
                    mappingCountCorrectionFactor = inbredElementMappingCountResultSet.getInt("mapping_count");

                    if(mappingCountCorrectionFactor == 0) {
                        mappingCountCorrectionFactor = 1;
                    }

                    inbredElementMappingCountResultSet.close();
                    inbredElementMappingCountStatement.close();
                    inbredElementMappingCountDatabaseConnection.close();
                }

                HashMap<Integer, Double> inbredExpressionHashMap = new HashMap();
                for(int inbredIndex : inbredTitleHashMap.keySet()) {
                    double inbredExpression = inbredElementExpressionResultSet.getDouble(inbredTitleHashMap.get(inbredIndex));
                    
                    inbredExpressionHashMap.put(inbredIndex, (inbredExpression / (double) mappingCountCorrectionFactor));
                }
    
                inbredElementExpressionHashMap[associationTypeIndex].put(inbredElementIndex, inbredExpressionHashMap);
            }

            inbredElementExpressionResultSet.close();
        }

        System.out.println("pos: " + inbredElementExpressionHashMap[0].size() + ", neg: " + inbredElementExpressionHashMap[1].size());
        
        this.performCorrelation(hybridCharacteristic, inbredElementExpressionHashMap, minExpressionThreshold, minFoldChangeThreshold);

        System.out.println();
        System.out.println("sequence ids: ");
        for(int inbredElementIndex : midParentalHeterosisAssociatedSequencesHashMap.keySet()) {
            if(midParentalHeterosisAssociatedSequencesHashMap.get(inbredElementIndex) > 0) {
                System.out.println(inbredElementIndex + "\tpositive");
            }
            else {
                System.out.println(inbredElementIndex + "\tnegative");
            }
        }

        inbredElementExpressionStatement.close();
        inbredElementExpressionDatabaseConnection.close();
    }
            
    public static void main(String[] args) {
        double fdrLimit = 0.05;
        double minimumExpression = 0.5;
        double minimumFoldChange = 2.0;
        
        int groupBalance = 0;
        int sequenceLength = 0;
                
        HybridCharacteristic hybridCharacteristic = HybridCharacteristic.MID_PARENT_HETEROSIS;
        
        MultipleTestingCorrectionMethod fdrCorrection = MultipleTestingCorrectionMethod.FDR_BENJAMINI_HOCHBERG;
        
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("-minimumExpression <expression> \tminimum expression value");
            System.out.println("-minimumFoldChange <fold change>\tminimum fold change");
            System.out.println("-characteristic {MPH|BPH|HP}");
            System.out.println("-sequenceLength <value>\tsingle sequence length (default: 0 -> no limitation)");
            System.out.println("-fdrCorrection {y|n}");
            System.out.println("-fdrLimit <value>\t(default: 0.05)");
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("minimumExpression")) {
                        minimumExpression = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("minimumFoldChange")) {
                        minimumFoldChange = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("characteristic")) {
                        if(argumentValue.equals("BPH")) {
                            System.out.println("best parent heterosis");
                            hybridCharacteristic = HybridCharacteristic.BEST_PARENT_HETEROSIS;
                        }
                        if(argumentValue.equals("MPH")) {
                            System.out.println("mid parent heterosis");
                            hybridCharacteristic = HybridCharacteristic.MID_PARENT_HETEROSIS;
                        }
                        else if(argumentValue.equals("HP")) {
                            System.out.println("hybrid performance");
                            hybridCharacteristic = HybridCharacteristic.HYBRID_PERFORMANCE;
                        }
                    }
                    else if(argumentTitle.equals("sequenceLength")) {
                        sequenceLength = Integer.valueOf(argumentValue);
                        System.out.println("sRNA length: " + ((sequenceLength == 0) ? "no limit" : (sequenceLength + " nt")));
                    }
                    else if(argumentTitle.equals("fdrCorrection")) {
                        fdrCorrection = (argumentValue.equals("n") ? MultipleTestingCorrectionMethod.NO_CORRECTION : MultipleTestingCorrectionMethod.FDR_BENJAMINI_HOCHBERG);
                    }
                    else if(argumentTitle.equals("fdrLimit")) {
                        fdrLimit = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    else if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                }
            }
        }
        
        if((databaseUser == null) || (databasePassword == null)) {
            System.out.println("-minimumExpression <expression> \tminimum expression value");
            System.out.println("-minimumFoldChange <fold change>\tminimum fold change");
            System.out.println("-characteristic {MPH|BPH|HP}");
            System.out.println("-sequenceLength <value>\tsingle sequence length (default: 0 -> no limitation)");
            System.out.println("-fdrCorrection {y|n}");
            System.out.println("-fdrLimit <value>\t(default: 0.05)");
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");
            System.exit(1);
        }
        
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
            HybridCharacteristicSequenceAssociationDatabase hybridCharacteristicSequenceAssociationDatabase = new HybridCharacteristicSequenceAssociationDatabase(databaseLoginData, fdrCorrection);

            hybridCharacteristicSequenceAssociationDatabase.testSequences(databaseLoginData, hybridCharacteristic, sequenceLength, minimumExpression, minimumFoldChange, SequenceMappingStatus.ALL, groupBalance, fdrLimit, false);
            
            hybridCharacteristicSequenceAssociationDatabase.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private HashMap<Integer,Integer[]> hybridInbredParentIndicesHashMap;
    
    private HashMap<Integer,String> inbredTitleHashMap;
    
    private final DatabaseConnection databaseConnection;
    
    private final MultipleTestingCorrectionMethod multipleTestingCorrectionMethod;
}