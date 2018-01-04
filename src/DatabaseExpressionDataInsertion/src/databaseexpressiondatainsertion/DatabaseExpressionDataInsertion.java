package databaseexpressiondatainsertion;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseExpressionDataInsertion {
    public static void main(String[] args) {
        String databasePassword = null;
        String databaseTable = null;
        String databaseUser = null;
        String expressionDataFile = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-databaseTable <table>\tthe name of the database table the expression data should be inserted into");
            System.out.println("-databaseUser <user>\tthe mysql user name");
            System.out.println("-databasePassword <password>\tthe password for the given mysql user");
            System.out.println("-expressionDataFile <file name>\tthe name of the file containing the merged expression data");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];

                    if(argumentTitle.equals("databaseTable")) {
                        databaseTable = argumentValue;
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                    if(argumentTitle.equals("expressionDataFile")) {
                        expressionDataFile = argumentValue;
                    }
                }
            }
        }
        
        if((databaseTable == null) || (databaseUser == null) || (databasePassword == null) || (expressionDataFile == null)) {
            System.out.println("Usage:");
            System.out.println("-databaseTable <table>\tthe name of the database table the expression data should be inserted into");
            System.out.println("-databaseUser <user>\tthe mysql user name");
            System.out.println("-databasePassword <password>\tthe password for the given mysql user");
            System.out.println("-expressionDataFile <file name>\tthe name of the file containing the merged expression data");
            System.exit(1);
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);

        try {
            DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
            Statement statement = databaseConnection.getConnection().createStatement();
            databaseConnection.executeQuery("DELETE FROM srna_heterosis." + databaseTable + " WHERE 1", statement);
            
            BufferedReader bufferedReader = new BufferedReader(new FileReader(expressionDataFile));
            
            boolean headerFlag = true;
            String[] datasetTitles = null;
            
            StringBuilder expressionDataInsertionStringBuilder = null;
            int sequenceCount = 0;
            
            String lineBuffer;
            while((lineBuffer = bufferedReader.readLine()) != null) {
                if(headerFlag) {
                    datasetTitles = lineBuffer.split("\t");
                    
                    headerFlag = false;
                    
                    continue;
                }
                
                if((sequenceCount % 10000) == 0) {
                    expressionDataInsertionStringBuilder = new StringBuilder("INSERT INTO srna_heterosis." + databaseTable + " (sequence_id");
                    for(int titleIndex = 1; titleIndex < datasetTitles.length; titleIndex++) {
                        expressionDataInsertionStringBuilder.append(", " +  datasetTitles[titleIndex].replace("srna_", "").replace(".csv", "").replace(".", ""));
                    }
                    expressionDataInsertionStringBuilder.append(") VALUES ");
                }
                else {
                    expressionDataInsertionStringBuilder.append(", ");
                }
                
                String[] dataParts = lineBuffer.split("\t");

                expressionDataInsertionStringBuilder.append("(" + sequenceCount);
                for(int dataIndex = 1; dataIndex < dataParts.length; dataIndex++) {
                    expressionDataInsertionStringBuilder.append(", " + dataParts[dataIndex]);
                }
                expressionDataInsertionStringBuilder.append(")");
                
                sequenceCount++;
                
                if((sequenceCount % 10000) == 0) {
                    databaseConnection.executeQuery(expressionDataInsertionStringBuilder.toString(), statement);
                }
            }   
            
            if((sequenceCount % 10000) > 0) {
                databaseConnection.executeQuery(expressionDataInsertionStringBuilder.toString(), statement);
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
