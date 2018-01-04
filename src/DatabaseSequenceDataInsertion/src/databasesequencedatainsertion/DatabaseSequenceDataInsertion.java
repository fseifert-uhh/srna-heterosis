package databasesequencedatainsertion;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSequenceDataInsertion {
    public static void main(String[] args) {
        String databaseUser = null;
        String databasePassword = null;
        String expressionDataFile = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-expressionDataFile <file name>\tthe name of the file containing the merged expression data");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    
                    if(argumentTitle.equals("expressionDataFile")) {
                        String argumentValue = args[argumentIndex];
                        expressionDataFile = argumentValue;
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        String argumentValue = args[argumentIndex];
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePasswort")) {
                        String argumentValue = args[argumentIndex];
                        databasePassword = argumentValue;
                    }
                }
            }
        }
        
        if((expressionDataFile == null) || (databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-expressionDataFile <file name>\tthe name of the file containing the merged expression data");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
            Statement statement = databaseConnection.getConnection().createStatement();
            databaseConnection.executeQuery("DELETE FROM srna_heterosis.srna_sequence WHERE 1", statement);
            
            BufferedReader bufferedReader = new BufferedReader(new FileReader(expressionDataFile));
            
            boolean headerFlag = true;
            String[] datasetTitles = null;
            
            StringBuilder sequenceInsertionStringBuilder = null;
            int sequenceCount = 0;
            
            String lineBuffer;
            while((lineBuffer = bufferedReader.readLine()) != null) {
                if(headerFlag) {
                    datasetTitles = lineBuffer.split("\t");
                    
                    headerFlag = false;
                    
                    continue;
                }
                
                if((sequenceCount % 10000) == 0) {
                    sequenceInsertionStringBuilder = new StringBuilder("INSERT INTO srna_heterosis.srna_sequence (sequence_id, sequence, length) VALUES ");
                }
                else {
                    sequenceInsertionStringBuilder.append(", ");
                }
                
                String[] dataParts = lineBuffer.split("\t");

                sequenceInsertionStringBuilder.append("(" + sequenceCount + ", \"" + dataParts[0] + "\", " + dataParts[0].length() + ")");

                sequenceCount++;
                
                if((sequenceCount % 10000) == 0) {
                    databaseConnection.executeQuery(sequenceInsertionStringBuilder.toString(), statement);
                }
            }   
            
            if((sequenceCount % 10000) > 0) {
                databaseConnection.executeQuery(sequenceInsertionStringBuilder.toString(), statement);
            }
            
            bufferedReader.close();
            
            databaseConnection.close();
        } catch (IOException e) {
            System.out.println(e);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
