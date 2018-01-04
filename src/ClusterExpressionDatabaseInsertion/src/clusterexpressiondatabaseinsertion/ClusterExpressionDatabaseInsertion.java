package clusterexpressiondatabaseinsertion;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class ClusterExpressionDatabaseInsertion {
    public ClusterExpressionDatabaseInsertion(DatabaseLoginData databaseLoginData) throws SQLException {
        this.sequenceLibraryTitles = this.getSequenceLibraryTitles();
    }
    
    public final String[] getSequenceLibraryTitles() throws SQLException {
        ArrayList<String> sequenceLibraryTitleList = new ArrayList();

        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceLibraryStatement = databaseConnection.getConnection().createStatement();

        ResultSet libraryResultSet = sequenceLibraryStatement.executeQuery("SELECT library_title FROM srna_libraries");
        while(libraryResultSet.next()) {
            sequenceLibraryTitleList.add(libraryResultSet.getString("library_title"));
        }
        libraryResultSet.close();

        sequenceLibraryStatement.close();

        String [] sequenceLibraryTitles = new String[sequenceLibraryTitleList.size()];
        sequenceLibraryTitleList.toArray(sequenceLibraryTitles);

        return sequenceLibraryTitles;
    }
    
    public void insertAnnotationClusterExpression() throws SQLException {
        HashMap<String,Integer> sequenceMappingCountHashMap = new HashMap();
        HashMap<String,Double[]> sequenceExpressionHashMap = new HashMap();

        System.out.println("loading read count data");
        DatabaseConnection sequenceDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);
        
        ResultSet sequenceDataResultSet = sequenceDataStatement.executeQuery("SELECT * FROM srna_mapping_status WHERE mapping_count>0");
        while(sequenceDataResultSet.next()) {
            String sequenceIndex = sequenceDataResultSet.getString("sequence_id");
            int mappingCount = sequenceDataResultSet.getInt("mapping_count");

            sequenceMappingCountHashMap.put(sequenceIndex, mappingCount);
        }
        sequenceDataResultSet.close();
        
        sequenceDataStatement.close();
        
        System.out.println("loading expression data");
        sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement();
        
        for(String sequenceIndex : sequenceMappingCountHashMap.keySet()) {
            sequenceDataResultSet = sequenceDataStatement.executeQuery("SELECT * FROM srna_library_expression where sequence_id=" + sequenceIndex);
            if(sequenceDataResultSet.next()) {
                if(sequenceMappingCountHashMap.get(sequenceIndex) != null) {
                    Double[] sequenceLibraryExpression = new Double[sequenceLibraryTitles.length];
                    for(int libraryArrayIndex = 0; libraryArrayIndex < sequenceLibraryTitles.length; libraryArrayIndex++) {
                        sequenceLibraryExpression[libraryArrayIndex] = sequenceDataResultSet.getDouble(sequenceLibraryTitles[libraryArrayIndex]);
                    }

                    sequenceExpressionHashMap.put(sequenceIndex, sequenceLibraryExpression);
                }
            }
            sequenceDataResultSet.close();
        }
        
        sequenceDataStatement.close();
        sequenceDataDatabaseConnection.close();

        DatabaseConnection clusterDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement clusterExpressionStatement = clusterDatabaseConnection.getConnection().createStatement(); 
        clusterDatabaseConnection.executeQuery("TRUNCATE cluster_srna_expression_distinct", clusterExpressionStatement);
        clusterDatabaseConnection.executeQuery("TRUNCATE cluster_srna_expression_distinct_repeat_norm", clusterExpressionStatement);
        clusterDatabaseConnection.executeQuery("TRUNCATE cluster_srna_expression", clusterExpressionStatement);
        clusterDatabaseConnection.executeQuery("TRUNCATE cluster_srna_expression_repeat_norm", clusterExpressionStatement);

        StringBuilder libraryTitleStringBuilder = new StringBuilder();
        for(int libraryArrayIndex = 0; libraryArrayIndex < sequenceLibraryTitles.length; libraryArrayIndex++) {
            libraryTitleStringBuilder.append("," + sequenceLibraryTitles[libraryArrayIndex]);
        }
        
        System.out.println("generating mapping count/expression data");
        
        StringBuilder distinctReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression_distinct (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
        StringBuilder distinctRepeatNormalizedReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression_distinct_repeat_norm (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
        StringBuilder totalReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
        StringBuilder totalRepeatNormalizedReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression_repeat_norm (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
        int clusterDatasetInsertionCount = 0;
                
        DatabaseConnection clusterClusterDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement clusterClusterStatement = clusterClusterDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        clusterClusterStatement.setFetchSize(Integer.MIN_VALUE); 
        ResultSet clusterClusterResultSet = clusterClusterStatement.executeQuery("SELECT * FROM cluster_data");
        while(clusterClusterResultSet.next()) {
            int clusterClusterIndex = clusterClusterResultSet.getInt("cluster_id");
            
            int[] distinctReadCount = new int[sequenceLibraryTitles.length];
            double[] distinctRepeatNormalizedReadCount = new double[sequenceLibraryTitles.length];
            double[] totalReadCount = new double[sequenceLibraryTitles.length];
            double[] totalRepeatNormalizedReadCount = new double[sequenceLibraryTitles.length];
            
            String[] clusterSequenceIndices = clusterClusterResultSet.getString("sequence_id_text").split(";");
            for(String sequenceIndex : clusterSequenceIndices) {
                if(!sequenceIndex.isEmpty()) {
                    Double[] sequenceLibraryExpression = sequenceExpressionHashMap.get(sequenceIndex);
                    
                    for(int libraryArrayIndex = 0; libraryArrayIndex < sequenceLibraryTitles.length; libraryArrayIndex++) {
                        if(sequenceLibraryExpression[libraryArrayIndex] > 0) {
                            distinctReadCount[libraryArrayIndex]++;
                            distinctRepeatNormalizedReadCount[libraryArrayIndex] += (1.0 / (double) sequenceMappingCountHashMap.get(sequenceIndex));
                            totalReadCount[libraryArrayIndex] += sequenceLibraryExpression[libraryArrayIndex];
                            totalRepeatNormalizedReadCount[libraryArrayIndex] += (sequenceLibraryExpression[libraryArrayIndex] / (double) sequenceMappingCountHashMap.get(sequenceIndex));
                        }
                    }
                }
            }
            
            if(clusterDatasetInsertionCount > 0) {
                distinctReadCountInsertionStringBuilder.append(",");
                distinctRepeatNormalizedReadCountInsertionStringBuilder.append(",");
                totalReadCountInsertionStringBuilder.append(",");
                totalRepeatNormalizedReadCountInsertionStringBuilder.append(",");
            }
            
            distinctReadCountInsertionStringBuilder.append("(" + clusterClusterIndex);
            distinctRepeatNormalizedReadCountInsertionStringBuilder.append("(" + clusterClusterIndex);
            totalReadCountInsertionStringBuilder.append("(" + clusterClusterIndex);
            totalRepeatNormalizedReadCountInsertionStringBuilder.append("(" + clusterClusterIndex);
            
            for(int libraryArrayIndex = 0; libraryArrayIndex < sequenceLibraryTitles.length; libraryArrayIndex++) {
                distinctReadCountInsertionStringBuilder.append("," + distinctReadCount[libraryArrayIndex]);
                distinctRepeatNormalizedReadCountInsertionStringBuilder.append("," + distinctRepeatNormalizedReadCount[libraryArrayIndex]);
                totalReadCountInsertionStringBuilder.append("," + totalReadCount[libraryArrayIndex]);
                totalRepeatNormalizedReadCountInsertionStringBuilder.append("," + totalRepeatNormalizedReadCount[libraryArrayIndex]);
            }
            
            distinctReadCountInsertionStringBuilder.append(")");
            distinctRepeatNormalizedReadCountInsertionStringBuilder.append(")");
            totalReadCountInsertionStringBuilder.append(")");
            totalRepeatNormalizedReadCountInsertionStringBuilder.append(")");
            clusterDatasetInsertionCount++;
            
            if(clusterDatasetInsertionCount == 1000) {
                clusterExpressionStatement.execute(distinctReadCountInsertionStringBuilder.toString());
                clusterExpressionStatement.execute(distinctRepeatNormalizedReadCountInsertionStringBuilder.toString());
                clusterExpressionStatement.execute(totalReadCountInsertionStringBuilder.toString());
                clusterExpressionStatement.execute(totalRepeatNormalizedReadCountInsertionStringBuilder.toString());
                
                distinctReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression_distinct (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
                distinctRepeatNormalizedReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression_distinct_repeat_norm (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
                totalReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
                totalRepeatNormalizedReadCountInsertionStringBuilder = new StringBuilder("INSERT INTO cluster_srna_expression_repeat_norm (cluster_id" + libraryTitleStringBuilder.toString() + ") VALUES ");
                clusterDatasetInsertionCount = 0;
            }
        }
        
        clusterClusterResultSet.close();
        clusterClusterStatement.close();
        clusterClusterDatabaseConnection.close();
        
        if(clusterDatasetInsertionCount > 0) {
            clusterExpressionStatement.execute(distinctReadCountInsertionStringBuilder.toString());
            clusterExpressionStatement.execute(distinctRepeatNormalizedReadCountInsertionStringBuilder.toString());
            clusterExpressionStatement.execute(totalReadCountInsertionStringBuilder.toString());
            clusterExpressionStatement.execute(totalRepeatNormalizedReadCountInsertionStringBuilder.toString());
        }
        
        clusterExpressionStatement.close();
        clusterDatabaseConnection.close();
    }
    
    public static void main(String[] args) {
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
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
                    
                    if(argumentTitle.equals("databaseUser")) {
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
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");
            System.exit(1);
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            ClusterExpressionDatabaseInsertion databaseAnnotationClusterGeneration = new ClusterExpressionDatabaseInsertion(databaseLoginData);
            databaseAnnotationClusterGeneration.insertAnnotationClusterExpression();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    private DatabaseLoginData databaseLoginData;
    
    private String[] sequenceLibraryTitles;
}
