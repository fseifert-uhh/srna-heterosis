package sequencesetannotationanalysis;

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

public class SequenceSetAnnotationAnalysis {
    public SequenceSetAnnotationAnalysis(DatabaseLoginData databaseLoginData, String sRNAFileName, String annotation) throws SQLException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(sRNAFileName));        
        
        HashMap<String,ArrayList<String>> annotationSequenceIndexHashMap = new HashMap();
        HashMap<String,ArrayList<String>> sequenceAnnotationIndexHashMap = new HashMap();
        
        String sRNADataLine = null;
        while((sRNADataLine = bufferedReader.readLine()) != null) {
            if(!sRNADataLine.isEmpty()) {
                String[] sRNADataLineParts = sRNADataLine.split("\t");
                
                String sequenceIndex = sRNADataLineParts[0];
                String annotationIndex = sRNADataLineParts[1];
                
                if(annotationSequenceIndexHashMap.get(annotationIndex) == null ) {
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
        
        Statement genomeAnnotationClassificationStatement = genomeAnnotationClassificationDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        genomeAnnotationClassificationStatement.setFetchSize(Integer.MIN_VALUE);

        ResultSet genomeAnnotationClassificationResultSet = genomeAnnotationClassificationStatement.executeQuery("SELECT * FROM genome_annotation_classification, genome_annotation WHERE genome_annotation_classification.annotation_id=genome_annotation.annotation_id");
        while(genomeAnnotationClassificationResultSet.next()) {
            String annotationIndex = genomeAnnotationClassificationResultSet.getString("annotation_id");
            
            if(annotationSequenceIndexHashMap.get(annotationIndex) != null) {
                String annotationType = genomeAnnotationClassificationResultSet.getString("sequence_type");
                String annotationText = null;
                        
                if((annotation == null) || annotationType.equals(annotation)) {
                    if(annotation == null) {
                        annotationText = "";
                    }
                    else if(annotation.equals("gene")) {
                        annotationText = genomeAnnotationClassificationResultSet.getString("annotation_text").split(";")[0].split("=")[1] + "\t";
                    }
                    else {
                        annotationText = annotation + "\t";
                    }
                    
                    System.out.print(annotationText + annotationIndex + "\t");
                    for(String associatedSequenceIndex : annotationSequenceIndexHashMap.get(annotationIndex)) {
                        System.out.print(";" + associatedSequenceIndex);
                    }
                    System.out.println(";");
                }
            }
        }
        genomeAnnotationClassificationResultSet.close();
        
        genomeAnnotationClassificationStatement.close();
        genomeAnnotationClassificationDatabaseConnection.close();
    }
    
    public static void main(String[] args) {
        String sRNAFileName = null;
        String annotation = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-filename <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
            System.out.println("-annotation <type> \ttype (optional): gene, repeat, intergenic");
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
            
            if((sRNAFileName == null) || (databaseUser == null) || (databasePassword == null)) {
                System.out.println("Usage:");
                System.out.println("-filename <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
                System.out.println("-annotation <type> \ttype (optional): gene, repeat, intergenic");
                System.out.println("-databaseUser <username>");
                System.out.println("-databasePassword <password>");
                System.exit(1);
            }
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", "srna_heterosis", "GBcrN9sD7CwLvw2a");
        try {
            SequenceSetAnnotationAnalysis sequenceSetAnnotationAnalysis = new SequenceSetAnnotationAnalysis(databaseLoginData, sRNAFileName, annotation);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
