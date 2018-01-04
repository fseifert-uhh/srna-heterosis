package hybridcharacteristicclusterassociationdatabase;

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

public class HybridCharacteristicClusterAssociationDatabase {
    public HybridCharacteristicClusterAssociationDatabase(DatabaseLoginData databaseLoginData, MultipleTestingCorrectionMethod multipleTestingCorrectionMethod) throws SQLException {
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
                    if(highInbredParentExpression >= minFoldChangeThreshold) {
                        hybridInbredParentDifferentialExpressionFlagHashMap.put(hybridIndex, true);
                    }
                }
                else if((highInbredParentExpression / lowInbredParentExpression) >= minFoldChangeThreshold) {
                    hybridInbredParentDifferentialExpressionFlagHashMap.put(hybridIndex, true);
                }
            }
        }
        
        return hybridInbredParentDifferentialExpressionFlagHashMap;
    }

    public HashMap<Integer,Double> getAssociatedClusters(HybridCharacteristic hybridCharacteristic, double minExpressionThreshold, double minFoldChange, double alphaError) throws SQLException {
        HashMap<Integer,Double> putativeAssociatedClusterHashMap = new HashMap();

        if(databaseConnection.isActive()) {
            HashMap<Integer,Double> hybridCharacteristicValueHashMap = getHybridCharacteristicValues(hybridCharacteristic);
            
            int clusterCount = 0;        
            Statement clusterExpressionStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            clusterExpressionStatement.setFetchSize(Integer.MIN_VALUE);

            ResultSet clusterExpressionResultSet = clusterExpressionStatement.executeQuery("SELECT * FROM cluster_srna_expression_repeat_norm");
            while(clusterExpressionResultSet.next()) {
                int clusterIndex = clusterExpressionResultSet.getInt("cluster_id");
                
                HashMap<Integer,Double> inbredClusterExpressionHashMap = new HashMap();
                for(int inbredIndex : inbredTitleHashMap.keySet()) {
                    double inbredLineClusterExpression = (clusterExpressionResultSet.getDouble(inbredTitleHashMap.get(inbredIndex)));
                    
                    inbredClusterExpressionHashMap.put(inbredIndex, inbredLineClusterExpression);
                }

                HashMap<Integer,Boolean> hybridParentDifferentialExpressionFlagHashMap = getHybridInbredParentDifferentialExpression(inbredClusterExpressionHashMap, minExpressionThreshold, minFoldChange);
                for(int hybridIndex : hybridInbredParentIndicesHashMap.keySet()) {
                    if(hybridParentDifferentialExpressionFlagHashMap.get(hybridIndex)) {
                        clusterCount++;

                        break;
                    }
                }
                
                HybridCharacteristicAssociationStatistic hybridCharacteristicAssociationStatistic = new HybridCharacteristicAssociationStatistic(hybridCharacteristicValueHashMap);
                double clusterAssociationProbability = hybridCharacteristicAssociationStatistic.getStatistic(hybridParentDifferentialExpressionFlagHashMap);

                if(Math.abs(clusterAssociationProbability) < alphaError) {
                    putativeAssociatedClusterHashMap.put(clusterIndex, clusterAssociationProbability);
                }
            }
            clusterExpressionResultSet.close();
            clusterExpressionStatement.close();
            
            if(putativeAssociatedClusterHashMap.size() > 0) {
                double[] candidateProbabilities = new double[putativeAssociatedClusterHashMap.size()];            
                int probabilityIndex = 0;
                for(double probabilityValue : putativeAssociatedClusterHashMap.values()) {
                    candidateProbabilities[probabilityIndex] = Math.abs(probabilityValue);
                    
                    probabilityIndex++;
                }
                
                MultipleTestingCorrection multipleTestingCorrection = new MultipleTestingCorrection(candidateProbabilities, clusterCount);
                double multipleTestingCorrectedAlphaError = multipleTestingCorrection.getCorrectedAlphaErrorThreshold(multipleTestingCorrectionMethod, alphaError);
                
                Object[] candidateSequenceIds = putativeAssociatedClusterHashMap.keySet().toArray();
                for (Object candidateSequenceId : candidateSequenceIds) {
                    if (Math.abs(putativeAssociatedClusterHashMap.get((Integer) candidateSequenceId)) > multipleTestingCorrectedAlphaError) {
                        putativeAssociatedClusterHashMap.remove((Integer) candidateSequenceId);
                    }
                }
            }
            
            System.out.println("number of DE clusters: " + clusterCount);
        }
        
        return putativeAssociatedClusterHashMap;
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
        String hybridCharacteristicDatabaseColumn;
        switch(hybridCharacteristic) {
            case BEST_PARENT_HETEROSIS:
                hybridCharacteristicDatabaseColumn = "best_parent_heterosis_r";
                break;
            case HYBRID_PERFORMANCE:
                hybridCharacteristicDatabaseColumn = "hybrid_performance";
                break;
            case MID_PARENT_HETEROSIS:
            default:
                hybridCharacteristicDatabaseColumn = "mid_parent_heterosis_r";
        }
        
        HashMap<Integer,Double> hybridCharacteristicValueHashMap = new HashMap();
        
        Statement hybridCharacteristicStatement = databaseConnection.getConnection().createStatement();
        
        ResultSet hybridCharacteristicResultSet = hybridCharacteristicStatement.executeQuery("SELECT * FROM hybrid_field_data");
        while(hybridCharacteristicResultSet.next()) {
            int hybridIndex = hybridCharacteristicResultSet.getInt("hybrid_id");
            double hybridCharacteristicValue = hybridCharacteristicResultSet.getDouble(hybridCharacteristicDatabaseColumn);

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
        
        for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
            if(inbredElementExpressionHashMap[0].isEmpty()) {
                hybridBinaryDistanceHashMap.put(hybridIndex, (1.0 - Math.sqrt(binaryDistanceHashMap[1].get(hybridIndex) / (double) inbredElementExpressionHashMap[1].size())));
                hybridEuclideanDistanceHashMap.put(hybridIndex, Math.sqrt(euclideanDistanceHashMap[1].get(hybridIndex)));
            }
            else if(inbredElementExpressionHashMap[1].isEmpty()) {
                hybridBinaryDistanceHashMap.put(hybridIndex, Math.sqrt(binaryDistanceHashMap[0].get(hybridIndex) / (double) inbredElementExpressionHashMap[0].size()));
                hybridEuclideanDistanceHashMap.put(hybridIndex, Math.sqrt(euclideanDistanceHashMap[0].get(hybridIndex)));
            }
            else {
                hybridBinaryDistanceHashMap.put(hybridIndex, (Math.sqrt(binaryDistanceHashMap[0].get(hybridIndex) / (double) inbredElementExpressionHashMap[0].size()) * ((double) inbredElementExpressionHashMap[0].size() / ((double) inbredElementExpressionHashMap[0].size() + (double) inbredElementExpressionHashMap[1].size()))) + ((1.0 - Math.sqrt(binaryDistanceHashMap[1].get(hybridIndex) / (double) inbredElementExpressionHashMap[1].size())) * ((double) inbredElementExpressionHashMap[1].size() / ((double) inbredElementExpressionHashMap[0].size() + (double) inbredElementExpressionHashMap[1].size()))));
                hybridEuclideanDistanceHashMap.put(hybridIndex, (Math.sqrt(euclideanDistanceHashMap[0].get(hybridIndex) + Math.sqrt(euclideanDistanceHashMap[1].get(hybridIndex)))));
            }

            System.out.println(hybridIndex + ";"  + hybridBinaryDistanceHashMap.get(hybridIndex) + ";" + hybridEuclideanDistanceHashMap.get(hybridIndex) + ";" + hybridCharacteristicValueHashMap.get(hybridIndex) + ";hi:;" + binaryDistanceHashMap[0].get(hybridIndex) + ";lo;" + binaryDistanceHashMap[1].get(hybridIndex));
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

        System.out.println(minExpressionThreshold + ";" + minFoldChangeThreshold + ";pos" + inbredElementExpressionHashMap[0].size() + ";neg" + inbredElementExpressionHashMap[1].size() + ";" + binaryDistanceHybridCharacteristicCorrelation.getR() + ";" + euclideanDistanceHybridCharacteristicCorrelation.getR());
    }
    
    public void testClusters(DatabaseLoginData databaseLoginData, double minExpressionThreshold, double minFoldChangeThreshold, double alphaError) throws SQLException {
        DatabaseConnection inbredElementExpressionDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement inbredElementExpressionStatement = inbredElementExpressionDatabaseConnection.getConnection().createStatement();
        
        HashMap<Integer,Double> midParentalHeterosisAssociatedClusterHashMap = getAssociatedClusters(HybridCharacteristic.MID_PARENT_HETEROSIS, minExpressionThreshold, minFoldChangeThreshold, alphaError);

        HashMap<Integer,HashMap<Integer,Double>>[] inbredElementExpressionHashMap = new HashMap[2];
        inbredElementExpressionHashMap[0] = new HashMap(); // positive association
        inbredElementExpressionHashMap[1] = new HashMap(); // negative association
        for(int inbredElementIndex : midParentalHeterosisAssociatedClusterHashMap.keySet()) {
            int associationTypeIndex = 0;
            if(midParentalHeterosisAssociatedClusterHashMap.get(inbredElementIndex) < 0) {
                associationTypeIndex = 1;
            }
            
            inbredElementExpressionHashMap[associationTypeIndex].put(inbredElementIndex, new HashMap());
            
            ResultSet inbredElementExpressionResultSet = inbredElementExpressionStatement.executeQuery("SELECT * FROM cluster_srna_expression_repeat_norm WHERE cluster_id=" + inbredElementIndex);
            if(inbredElementExpressionResultSet.next()) {

                HashMap<Integer, Double> inbredExpressionHashMap = new HashMap();
                for(int inbredIndex : inbredTitleHashMap.keySet()) {
                    double inbredExpression = inbredElementExpressionResultSet.getDouble(inbredTitleHashMap.get(inbredIndex));
                    
                    inbredExpressionHashMap.put(inbredIndex, inbredExpression);
                }
    
                inbredElementExpressionHashMap[associationTypeIndex].put(inbredElementIndex, inbredExpressionHashMap);
            }

            inbredElementExpressionResultSet.close();
        }

        this.performCorrelation(HybridCharacteristic.MID_PARENT_HETEROSIS, inbredElementExpressionHashMap, minExpressionThreshold, minFoldChangeThreshold);

        System.out.println();
        System.out.println("annotation cluster ids: ");
        for(int inbredElementIndex : midParentalHeterosisAssociatedClusterHashMap.keySet()) {
            if(midParentalHeterosisAssociatedClusterHashMap.get(inbredElementIndex) > 0) {
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
        double minimumExpression = 5.0;
        double minimumFoldChange = 2.0;
        
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-minimumExpression <expression> \tminimum expression value");
            System.out.println("-minimumFoldChange <fold change>\tminimum fold change");
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
                    
                    if(argumentTitle.equals("minimumExpression")) {
                        minimumExpression = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("minimumFoldChange")) {
                        minimumFoldChange = Double.valueOf(argumentValue);
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
            System.out.println("Usage:");
            System.out.println("-minimumExpression <expression> \tminimum expression value");
            System.out.println("-minimumFoldChange <fold change>\tminimum fold change");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
            HybridCharacteristicClusterAssociationDatabase hybridCharacteristicClusterAssociationDatabase = new HybridCharacteristicClusterAssociationDatabase(databaseLoginData, MultipleTestingCorrectionMethod.FDR_BENJAMINI_HOCHBERG);

            hybridCharacteristicClusterAssociationDatabase.testClusters(databaseLoginData, minimumExpression, minimumFoldChange, 0.05);
            
            hybridCharacteristicClusterAssociationDatabase.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private HashMap<Integer,Integer[]> hybridInbredParentIndicesHashMap;
    
    private HashMap<Integer,String> inbredTitleHashMap;
    
    private final DatabaseConnection databaseConnection;
    
    private final MultipleTestingCorrectionMethod multipleTestingCorrectionMethod;
}