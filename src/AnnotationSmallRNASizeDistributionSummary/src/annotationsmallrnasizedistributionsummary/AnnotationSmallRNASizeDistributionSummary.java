package annotationsmallrnasizedistributionsummary;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class AnnotationSmallRNASizeDistributionSummary {
    public AnnotationSmallRNASizeDistributionSummary(String databaseUser, String databasePassword) {
        databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
    }
    
    public void extractSmallRNASummary(String annotationResultFileName, String title) throws IOException, SQLException {
        HashMap<String,Boolean> smallRNAIndexHashMap = new HashMap();
        
        String fileLine;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(annotationResultFileName));
        while((fileLine = bufferedReader.readLine()) != null) {
            String smallRNAIndex = fileLine.split("\t")[1].replace(";", "");
            
            smallRNAIndexHashMap.put(smallRNAIndex, Boolean.TRUE);
        }
        bufferedReader.close();
        
        int[] sequenceLengthSummary = new int[11];
        
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceLengthStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceLengthStatement.setFetchSize(Integer.MIN_VALUE);

        ResultSet sequenceLengthResultSet = sequenceLengthStatement.executeQuery("SELECT * FROM srna_sequence");
        while(sequenceLengthResultSet.next()) {
            String sequenceIndex = sequenceLengthResultSet.getString("sequence_id");
            int sequenceLength = sequenceLengthResultSet.getInt("length");
            
            if(smallRNAIndexHashMap.get(sequenceIndex) != null) {
                sequenceLengthSummary[sequenceLength - 18]++;
            }
        }
        sequenceLengthResultSet.close();
        
        sequenceLengthStatement.close();
        databaseConnection.close();
        
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthSummary.length; sequenceLengthIndex++) {
            System.out.print("\t" + (sequenceLengthIndex + 18));
        }
        System.out.println();
        System.out.print(title);
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < sequenceLengthSummary.length; sequenceLengthIndex++) {
            System.out.print("\t" + sequenceLengthSummary[sequenceLengthIndex]);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        String annotationResultFileName = null;
        String title = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-result <filename>");
            System.out.println("-title <name>");
            System.out.println("-databasUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("result")) {
                        annotationResultFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("title")) {
                        title = argumentValue;
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
        
        if((annotationResultFileName == null) || (title == null) || (databasePassword == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-result <filename>");
            System.out.println("-title <name>");
            System.out.println("-databasUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        
        AnnotationSmallRNASizeDistributionSummary annotationSmallRNASizeDistributionSummary = new AnnotationSmallRNASizeDistributionSummary(databaseUser, databasePassword);
        try {
            annotationSmallRNASizeDistributionSummary.extractSmallRNASummary(annotationResultFileName, title);
        }
        catch(IOException e) {
            System.out.println(e);
        }
        catch(SQLException e) {
            System.out.println(e);
        }
    }
    
    private DatabaseLoginData databaseLoginData;
}
