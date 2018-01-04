package sequencesetannotationspecificanalysis;

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

public class SequenceSetAnnotationSpecificAnalysis {
    public SequenceSetAnnotationSpecificAnalysis(DatabaseLoginData databaseLoginData, String sRNAFileName, String annotation) throws SQLException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(sRNAFileName));        
        
        HashMap<String,ArrayList<String>> annotationSequenceIndexHashMap = new HashMap();
        HashMap<String,ArrayList<String>> sequenceAnnotationIndexHashMap = new HashMap();
        
        String sRNADataLine = null;
        while((sRNADataLine = bufferedReader.readLine()) != null) {
            if(!sRNADataLine.isEmpty()) {
                String[] sRNADataLineParts = sRNADataLine.split("\t");
                
                String sequenceIndex = sRNADataLineParts[0];
                String annotationIndex = sRNADataLineParts[1];
                
                if(annotationSequenceIndexHashMap.get(annotationIndex) == null) {
                    annotationSequenceIndexHashMap.put(annotationIndex, new ArrayList());
                }
                
                ArrayList<String> sequenceIndexArrayList = annotationSequenceIndexHashMap.get(annotationIndex);
                sequenceIndexArrayList.add(sequenceIndex);
                annotationSequenceIndexHashMap.put(annotationIndex, sequenceIndexArrayList);
                
                if(sequenceAnnotationIndexHashMap.get(sequenceIndex) == null) {
                    sequenceAnnotationIndexHashMap.put(sequenceIndex, new ArrayList());
                }
                
                ArrayList<String> annotationIndexArrayList = sequenceAnnotationIndexHashMap.get(sequenceIndex);
                annotationIndexArrayList.add(annotationIndex);
                sequenceAnnotationIndexHashMap.put(sequenceIndex, annotationIndexArrayList);
            }
        }
        
        bufferedReader.close();
        
        DatabaseConnection genomeAnnotationClassificationDatabaseConnection = new DatabaseConnection(databaseLoginData);
        
        HashMap<String,String> annotationTypeHashMap = new HashMap();
        HashMap<String,Integer[]> annotationTypeSummaryHashMap = new HashMap();
        
        Statement genomeAnnotationTypeStatement = genomeAnnotationClassificationDatabaseConnection.getConnection().createStatement();
        ResultSet genomeAnnotationTypeResultSet = genomeAnnotationTypeStatement.executeQuery("SELECT * FROM annotation_type_data");
        while(genomeAnnotationTypeResultSet.next()) {
            String annotationTypeIndex = genomeAnnotationTypeResultSet.getString("annotation_type_id");
            String annotationTypeTitle = genomeAnnotationTypeResultSet.getString("title");
            
            annotationTypeHashMap.put(annotationTypeTitle, annotationTypeIndex);
        }
        
        Statement genomeAnnotationClassificationStatement = genomeAnnotationClassificationDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        genomeAnnotationClassificationStatement.setFetchSize(Integer.MIN_VALUE);

        HashMap<String,String> annotationClusterTypeHashMap = new HashMap();
        
        ResultSet genomeAnnotationClassificationResultSet = genomeAnnotationClassificationStatement.executeQuery("SELECT * FROM genome_annotation_classification");
        while(genomeAnnotationClassificationResultSet.next()) {
            String annotationIndex = genomeAnnotationClassificationResultSet.getString("annotation_id");
            
            if(annotationSequenceIndexHashMap.get(annotationIndex) != null) {
                String annotationType = genomeAnnotationClassificationResultSet.getString("annotation_type_id");
                
                annotationClusterTypeHashMap.put(annotationIndex, annotationType);
            }
        }
            
        genomeAnnotationClassificationResultSet.close();
        
        genomeAnnotationClassificationStatement.close();
        genomeAnnotationClassificationDatabaseConnection.close();
        
        DatabaseConnection genomeAnnotationDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement genomeAnnotationStatement = genomeAnnotationDatabaseConnection.getConnection().createStatement();
        
        for(String sequenceIndex : sequenceAnnotationIndexHashMap.keySet()) {
            boolean specificAnnotationClassFlag = true;
            
            for(String annotationIndex : sequenceAnnotationIndexHashMap.get(sequenceIndex)) {
                if((annotationClusterTypeHashMap.get(annotationIndex) == null) || !annotationClusterTypeHashMap.get(annotationIndex).equals(annotationTypeHashMap.get(annotation))) {
                    specificAnnotationClassFlag = false;
                    
                    break;
                }
            }
            
            if(specificAnnotationClassFlag) {
                System.out.print(annotation + "\t" + sequenceIndex);
                
                if(annotation.equals("gene")) {
                    System.out.print("\t");
                    
                    for(String annotationIndex : sequenceAnnotationIndexHashMap.get(sequenceIndex)) {
                        ResultSet genomeAnnotationResultSet = genomeAnnotationStatement.executeQuery("SELECT annotation_text FROM genome_annotation WHERE annotation_id=" + annotationIndex);
                        if(genomeAnnotationResultSet.next()) {
                            String annotationText = genomeAnnotationResultSet.getString("annotation_text").split(";")[0].split("=")[1];
                            
                            System.out.print(";" + annotationText);
                        }
                    }
                }
                
                System.out.println(";");
            }
        }
        
        genomeAnnotationStatement.close();
        genomeAnnotationDatabaseConnection.close();
    }
    
    public static void main(String[] args) {
        String sRNAFileName = null;
        String annotation = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Parameters: ");
            System.out.println("-filename <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
            System.out.println("-annotation <type> \ttype: gene, repeat, intergenic");
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
                    
                    if(argumentTitle.equals("filename")) {
                        sRNAFileName = argumentValue;
                    }
                    if(argumentTitle.equals("annotation")) {
                        annotation = argumentValue;
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                }
            }
            
            if((sRNAFileName == null) || (annotation == null) || (databaseUser == null) || (databasePassword == null)) {
                System.out.println("Parameters: ");
                System.out.println("-filename <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
                System.out.println("-annotation <type> \ttype: gene, repeat, intergenic");
                System.out.println("-databaseUser <username>");
                System.out.println("-databasePassword <password>");
                System.exit(1);
            }
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        try {
            SequenceSetAnnotationSpecificAnalysis sequenceSetAnnotationSpecificAnalysis = new SequenceSetAnnotationSpecificAnalysis(databaseLoginData, sRNAFileName, annotation);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
