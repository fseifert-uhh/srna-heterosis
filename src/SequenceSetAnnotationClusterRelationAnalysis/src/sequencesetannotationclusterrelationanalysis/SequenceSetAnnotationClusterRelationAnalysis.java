package sequencesetannotationclusterrelationanalysis;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SequenceSetAnnotationClusterRelationAnalysis {
    public SequenceSetAnnotationClusterRelationAnalysis(DatabaseLoginData databaseLoginData, String sRNAFileName) throws SQLException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(sRNAFileName));        
        
        HashMap<String,Boolean> sequenceIndexHashMap = new HashMap();
        
        String sRNADataLine = null;
        while((sRNADataLine = bufferedReader.readLine()) != null) {
            if(!sRNADataLine.isEmpty()) {
                sequenceIndexHashMap.put(sRNADataLine, true);
            }
        }
        
        bufferedReader.close();
        
        DatabaseConnection annotationClusterDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement annotationClusterStatement = annotationClusterDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        annotationClusterStatement.setFetchSize(Integer.MIN_VALUE);
        
        String strandSQL = "genome_annotation_srna_cluster.antisense=0"; 

        ResultSet annotationClusterResultSet = annotationClusterStatement.executeQuery("SELECT * FROM genome_annotation_srna_cluster, genome_annotation_classification, genome_annotation WHERE " + strandSQL + " AND genome_annotation_srna_cluster.annotation_id=genome_annotation.annotation_id AND genome_annotation.annotation_id=genome_annotation_classification.annotation_id");
        while(annotationClusterResultSet.next()) {
            int annotationClusterIndex = annotationClusterResultSet.getInt("annotation_id");
            String annotationClusterSequenceIndicesText = annotationClusterResultSet.getString("sequence_id_text");

            for(String annotationClusterSequenceIndex : annotationClusterSequenceIndicesText.split(";")) {
                if(!annotationClusterSequenceIndex.isEmpty()) {
                    if(sequenceIndexHashMap.get(annotationClusterSequenceIndex) != null) {
                        System.out.println(annotationClusterSequenceIndex + "\t" + annotationClusterIndex);
                    }
                }
            }
        }
        annotationClusterResultSet.close();
        
        annotationClusterStatement.close();
        annotationClusterDatabaseConnection.close();
    }
    
    public static void main(String[] args) {
        String databaseUser = null;
        String databasePassword = null;
        String sRNAFileName = null;
        
        if(args.length == 0) {
            System.out.println("Parameters: ");
            System.out.println("-sRNAFilename <path/filename> \tCSV-file (tab-separated) containing the sRNA ids in first column");
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
                    
                    if(argumentTitle.equals("sRNAFilename")) {
                        sRNAFileName = argumentValue;
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                }
            }
            
            if((sRNAFileName == null) || (databaseUser == null) || (databasePassword == null)) {
                System.out.println("Parameters: ");
                System.out.println("-sRNAFilename <path/filename> \tCSV-file (tab-separated) containing the sRNA ids in first column");
                System.out.println("-databaseUser <username>");
                System.out.println("-databasePassword <password>");
                System.exit(1);
            }
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        try {
            SequenceSetAnnotationClusterRelationAnalysis sequenceSetAnnotationAnalysis = new SequenceSetAnnotationClusterRelationAnalysis(databaseLoginData, sRNAFileName);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
