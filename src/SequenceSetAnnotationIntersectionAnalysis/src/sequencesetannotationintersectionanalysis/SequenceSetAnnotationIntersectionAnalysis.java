package sequencesetannotationintersectionanalysis;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SequenceSetAnnotationIntersectionAnalysis {
    public SequenceSetAnnotationIntersectionAnalysis(DatabaseLoginData databaseLoginData, String geneSmallRNAFileName, String intergenicSmallRNAFileName, String repeatSmallRNAFileName, String annotationIntersect) throws SQLException, IOException {
        int annotationIntersectIndex = 0;
        StringBuilder intersectTitleStringBuilder = new StringBuilder();
        if(annotationIntersect.contains("gene")) {
            annotationIntersectIndex += 1;
            intersectTitleStringBuilder.append("gene");
        }
        if(annotationIntersect.contains("intergenic")) {
            annotationIntersectIndex += 2;
            
            if(intersectTitleStringBuilder.length() > 0) {
                intersectTitleStringBuilder.append("/");
            }
            intersectTitleStringBuilder.append("intergenic");
        }
        if(annotationIntersect.contains("repeat")) {
            annotationIntersectIndex += 4;

            if(intersectTitleStringBuilder.length() > 0) {
                intersectTitleStringBuilder.append("/");
            }
            intersectTitleStringBuilder.append("repeat");
        }

        HashMap<String,Integer> annotationIntersectSequenceHashMap = new HashMap();
        
        String[] annotationSmallRNAFileNames = {geneSmallRNAFileName, intergenicSmallRNAFileName, repeatSmallRNAFileName};
        for(int annotationIndex = 0; annotationIndex <= 2; annotationIndex++) {
            HashMap<String,Boolean> annotationSmallRNAIndexHashMap = new HashMap();
        
            BufferedReader bufferedReader = new BufferedReader(new FileReader(annotationSmallRNAFileNames[annotationIndex]));        
            
            String sRNADataLine;
            while((sRNADataLine = bufferedReader.readLine()) != null) {
                if(!sRNADataLine.isEmpty()) {
                    String[] smallRNAIndices = sRNADataLine.split("\t")[2].split(";");
                    
                    for(String smallRNAIndex : smallRNAIndices) {
                        if(smallRNAIndex.isEmpty()) {
                            continue;
                        }
                        
                        annotationSmallRNAIndexHashMap.put(smallRNAIndex, Boolean.TRUE);
                    }
                }
            }
            
            bufferedReader.close();
            
            for(String smallRNAIndex : annotationSmallRNAIndexHashMap.keySet()) {
                int currentIntersectValue = (int) Math.pow(2, annotationIndex);
                
                if(annotationIntersectSequenceHashMap.get(smallRNAIndex) != null) {
                    currentIntersectValue += annotationIntersectSequenceHashMap.get(smallRNAIndex);
                }

                annotationIntersectSequenceHashMap.put(smallRNAIndex, currentIntersectValue);
            }
        }
        
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceLengthStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceLengthStatement.setFetchSize(Integer.MIN_VALUE);

        ResultSet sequenceLengthResultSet = sequenceLengthStatement.executeQuery("SELECT * FROM srna_sequence");
        while(sequenceLengthResultSet.next()) {
            String sequenceIndex = sequenceLengthResultSet.getString("sequence_id");
            int sequenceLength = sequenceLengthResultSet.getInt("length");
            
            Integer intersectIndex = annotationIntersectSequenceHashMap.get(sequenceIndex);
            if(intersectIndex != null) {
                if(intersectIndex == annotationIntersectIndex) {
                    System.out.println(intersectTitleStringBuilder.toString() + "\t" + sequenceIndex);
                }
            }
        }
        sequenceLengthResultSet.close();
        
        sequenceLengthStatement.close();
        databaseConnection.close();
    }
    
    public static void main(String[] args) {
        String annotationIntersect = null;
        String geneSmallRNAFileName = null;
        String intergenicSmallRNAFileName = null;
        String repeatSmallRNAFileName = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Parameters: ");
            System.out.println("-intersect <annotation> \tannotations to be intersected separated by comma (e.g. gene,repeat)");
            System.out.println("-geneFile <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
            System.out.println("-intergenicFile <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
            System.out.println("-repeatFile <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
            System.out.println("-databaseUser <user>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("intersect")) {
                        annotationIntersect = argumentValue;
                    }
                    if(argumentTitle.equals("geneFile")) {
                        geneSmallRNAFileName = argumentValue;
                    }
                    if(argumentTitle.equals("intergenicFile")) {
                        intergenicSmallRNAFileName = argumentValue;
                    }
                    if(argumentTitle.equals("repeatFile")) {
                        repeatSmallRNAFileName = argumentValue;
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                }
            }
            
            if(((geneSmallRNAFileName == null) && (intergenicSmallRNAFileName == null) && (repeatSmallRNAFileName == null)) || (databaseUser == null) || (databasePassword == null)) {
                System.out.println("Parameters: ");
                System.out.println("-intersect <annotation> \tannotations to be intersected separated by comma (e.g. gene,repeat)");
                System.out.println("-geneFile <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
                System.out.println("-intergenicFile <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
                System.out.println("-repeatFile <path/filename> \tCSV-file (tab-separated) containing the sRNA index in first column followed by annotation index");
                System.out.println("-databaseUser <user>");
                System.out.println("-databasePassword <password>");
                System.exit(1);
            }
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        try {
            SequenceSetAnnotationIntersectionAnalysis sequenceSetAnnotationIntersectionAnalysis = new SequenceSetAnnotationIntersectionAnalysis(databaseLoginData, geneSmallRNAFileName, intergenicSmallRNAFileName, repeatSmallRNAFileName, annotationIntersect);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
